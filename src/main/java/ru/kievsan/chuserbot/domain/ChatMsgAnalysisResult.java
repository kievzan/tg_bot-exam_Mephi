package ru.kievsan.chuserbot.domain;

import java.util.Set;

/**
 * Результат анализа чата: уникальные участники и упоминания.
 */
public record ChatMsgAnalysisResult(Set<Member> members, Set<Mention> mentions) {

    public ChatMsgAnalysisResult {
        if (members == null) {
            throw new IllegalArgumentException("members cannot be null");
        }
        if (mentions == null) {
            throw new IllegalArgumentException("mentions cannot be null");
        }
    }

    /**
     * Получить количество уникальных участников.
     */
    public int getMembersCount() {
        return members.size();
    }

    /**
     * Получить количество уникальных упоминаний.
     */
    public int getMentionsCount() {
        return mentions.size();
    }

    /**
     * Получить общее количество уникальных сущностей (участники + упоминания).
     */
    public int getTotalCount() {
        return getMembersCount() + getMentionsCount();
    }
}
