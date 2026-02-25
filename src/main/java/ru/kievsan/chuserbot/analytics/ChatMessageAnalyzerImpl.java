package ru.kievsan.chuserbot.analytics;

import lombok.extern.slf4j.Slf4j;
import ru.kievsan.chuserbot.domain.ChatMsgAnalysisResult;
import ru.kievsan.chuserbot.domain.ChatExport;
import ru.kievsan.chuserbot.domain.Member;
import ru.kievsan.chuserbot.domain.Mention;

import java.util.HashSet;
import java.util.Set;

/**
 * Анализатора чата - извлекает участников и упоминания из экспорта сообщений чата.
 */
@Slf4j
public class ChatMessageAnalyzerImpl implements ChatMessageAnalyzer {

    private static final String DELETED_ACCOUNT_NAME_EN = "Deleted account";
    private static final String DELETED_ACCOUNT_NAME_RU = "Удалённый аккаунт";
    private static final String MENTION_TYPE_NAME = "mention";

    @Override
    public ChatMsgAnalysisResult analyze(ChatExport chatExport) throws ChatAnalysisException {
        if (chatExport == null) {
            throw new ChatAnalysisException("ChatExport cannot be null");
        }

        if (chatExport.getMessages() == null) {
            log.warn("ChatExport has null messages list, returning empty result");
            return new ChatMsgAnalysisResult(Set.of(), Set.of());
        }

        Set<Member> members = new HashSet<>();
        Set<Mention> mentions = new HashSet<>();

        for (ChatExport.Message message : chatExport.getMessages()) {
            if (message == null) {
                continue;
            }

            // Извлечение участника, не удалённый аккаунт
            extractParticipant(message, members);

            // Извлечение упоминания
            extractMentions(message, mentions);
        }

        log.info("Analysis completed: {} members, {} mentions", members.size(), mentions.size());
        return new ChatMsgAnalysisResult(members, mentions);
    }

    /**
     * Извлечь участника из сообщения (если он не является удалённым аккаунтом).
     */
    private void extractParticipant(ChatExport.Message message, Set<Member> members) {
        String from = message.getFrom();
        String fromId = message.getFromId();

        // Пропускаем, когда:
        // нет обязательных полей,
        // fromId - не null и не blank,
        // from - не null.
        if (fromId == null || fromId.isBlank() || from == null) {
            return;
        }

        if (isDeletedAccount(from)) {
            log.info("Пропускаем удалённые аккаунты: fromId={}, from={}", fromId, from);
            return;
        }

        try {
            members.add(new Member(fromId, from));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create Member: fromId={}, from={}, error={}", fromId, from, e.getMessage());
        }
    }

    /**
     * Извлечь упоминания из сообщения.
     */
    private void extractMentions(ChatExport.Message message, Set<Mention> mentions) {
        if (message.getTextEntities() == null) {
            return;
        }

        for (ChatExport.TextEntity entity : message.getTextEntities()) {
            if (entity == null) {
                continue;
            }

            // Поиск сущностей "mention".
            if (MENTION_TYPE_NAME.equals(entity.getType()) && entity.getText() != null) {
                String mentionText = entity.getText().trim();
                if (!mentionText.isBlank()) {
                    try {
                        mentions.add(new Mention(mentionText));
                    } catch (IllegalArgumentException e) {
                        log.warn("Failed to create Mention: text={}, error={}", mentionText, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Проверить, является ли аккаунт удалённым.
     */
    private boolean isDeletedAccount(String from) {
        if (from == null) {
            return false;
        }
        String fromLower = from.toLowerCase().trim();
        return fromLower.equals(DELETED_ACCOUNT_NAME_EN.toLowerCase())
                || fromLower.equals(DELETED_ACCOUNT_NAME_RU.toLowerCase());
    }
}
