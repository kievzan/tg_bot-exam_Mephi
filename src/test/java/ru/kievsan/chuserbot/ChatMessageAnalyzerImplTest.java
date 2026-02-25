package ru.kievsan.chuserbot;

import org.junit.jupiter.api.Test;
import ru.kievsan.chuserbot.analytics.ChatMessageAnalyzer;
import ru.kievsan.chuserbot.analytics.ChatMessageAnalyzerImpl;
import ru.kievsan.chuserbot.domain.ChatMsgAnalysisResult;
import ru.kievsan.chuserbot.domain.ChatExport;
import ru.kievsan.chuserbot.domain.Member;
import ru.kievsan.chuserbot.domain.Mention;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageAnalyzerImplTest {

    private final ChatMessageAnalyzer analyzer = new ChatMessageAnalyzerImpl();

    @Test
    void extractParticipantsAndMentions() throws Exception {
        ChatExport chatExport = createTestChatExport();

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(2, result.getMembersCount());
        assertEquals(2, result.getMentionsCount());
        assertEquals(4, result.getTotalCount());
    }

    @Test
    void extractCorrectMembers() throws Exception {
        ChatExport chatExport = createTestChatExport();

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);
        Set<Member> members = result.members();

        assertEquals(2, members.size());
        assertTrue(members.contains(new Member("user123456789", "Сергей Киевский")));
        assertTrue(members.contains(new Member("user789456123", "Sergey Kievskiy")));
    }

    @Test
    void extractCorrectMentions() throws Exception {
        ChatExport chatExport = createTestChatExport();

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);
        Set<Mention> mentions = result.mentions();

        assertEquals(2, mentions.size());
        assertTrue(mentions.contains(new Mention("@skievskiy")));
        assertTrue(mentions.contains(new Mention("@skievskiywork")));
    }

    @Test
    void ignoreDeletedAccounts() throws Exception {
        ChatExport.Message deletedAccountMessage = new ChatExport.Message();
        deletedAccountMessage.setFrom("Deleted Account");
        deletedAccountMessage.setFromId("user000");

        ChatExport.Message normalMessage = new ChatExport.Message();
        normalMessage.setFrom("Нормальный Пользователь");
        normalMessage.setFromId("user111");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(deletedAccountMessage, normalMessage));

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getMembersCount());
        assertTrue(result.members().contains(new Member("user111", "Нормальный Пользователь")));
    }

    @Test
    void ignoreDeletedAccountsRussian() throws Exception {
        ChatExport.Message deletedAccountMessage = new ChatExport.Message();
        deletedAccountMessage.setFrom("Удалённый аккаунт");
        deletedAccountMessage.setFromId("user000");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(deletedAccountMessage));

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(0, result.getMembersCount());
    }

    @Test
    void ignoreMessagesWithoutFromOrFromId() throws Exception {
        ChatExport.Message messageWithoutFrom = new ChatExport.Message();
        messageWithoutFrom.setFromId("user111");

        ChatExport.Message messageWithoutFromId = new ChatExport.Message();
        messageWithoutFromId.setFrom("Пользователь");

        ChatExport.Message normalMessage = new ChatExport.Message();
        normalMessage.setFrom("Нормальный Пользователь");
        normalMessage.setFromId("user222");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(messageWithoutFrom, messageWithoutFromId, normalMessage));

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getMembersCount());
        assertTrue(result.members().contains(new Member("user222", "Нормальный Пользователь")));
    }

    @Test
    void handleMemberWithBlankDisplayName() throws Exception {
        ChatExport.Message messageWithBlankName = new ChatExport.Message();
        messageWithBlankName.setFrom("");
        messageWithBlankName.setFromId("user000");

        ChatExport.Message messageWithWhitespaceName = new ChatExport.Message();
        messageWithWhitespaceName.setFrom("   ");
        messageWithWhitespaceName.setFromId("user888");

        ChatExport.Message normalMessage = new ChatExport.Message();
        normalMessage.setFrom("Нормальный Пользователь");
        normalMessage.setFromId("user222");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(messageWithBlankName, messageWithWhitespaceName, normalMessage));

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        // здесь - все три участника (blank имя - валидное).
        assertEquals(3, result.getMembersCount());
        assertTrue(result.members().contains(new Member("user000", "")));
        assertTrue(result.members().contains(new Member("user888", "   ")));
        assertTrue(result.members().contains(new Member("user222", "Нормальный Пользователь")));
    }

    @Test
    void handleEmptyMessagesList() throws Exception {
        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of());

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(0, result.getMembersCount());
        assertEquals(0, result.getMentionsCount());
        assertEquals(0, result.getTotalCount());
    }

    @Test
    void handleNullMessagesList() throws Exception {
        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(null);

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(0, result.getMembersCount());
        assertEquals(0, result.getMentionsCount());
    }

    @Test
    void handleNullMessage() throws Exception {
        ChatExport.Message normalMessage = new ChatExport.Message();
        normalMessage.setFrom("Пользователь");
        normalMessage.setFromId("user111");

        ChatExport chatExport = new ChatExport();
        List<ChatExport.Message> messages = new java.util.ArrayList<>();
        messages.add(null);
        messages.add(normalMessage);
        chatExport.setMessages(messages);

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getMembersCount());
    }

    @Test
    void handleMessagesWithoutTextEntities() throws Exception {
        ChatExport.Message message = new ChatExport.Message();
        message.setFrom("Пользователь");
        message.setFromId("user111");
        message.setTextEntities(null);

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(message));

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getMembersCount());
        assertEquals(0, result.getMentionsCount());
    }

    @Test
    void notCountDuplicateMembers() throws Exception {
        ChatExport.Message message1 = new ChatExport.Message();
        message1.setFrom("Пользователь");
        message1.setFromId("user111");

        ChatExport.Message message2 = new ChatExport.Message();
        message2.setFrom("Пользователь");
        message2.setFromId("user111");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(message1, message2));

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getMembersCount());
    }

    @Test
    void notCountDuplicateMentions() throws Exception {
        ChatExport.TextEntity mention1 = new ChatExport.TextEntity();
        mention1.setType("mention");
        mention1.setText("@username");

        ChatExport.TextEntity mention2 = new ChatExport.TextEntity();
        mention2.setType("mention");
        mention2.setText("@username");

        ChatExport.Message message = new ChatExport.Message();
        message.setFrom("Пользователь");
        message.setFromId("user111");
        message.setTextEntities(List.of(mention1, mention2));

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(message));

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getMentionsCount());
    }

    @Test
    void onlyExtractMentionType() throws Exception {
        ChatExport.TextEntity plain = new ChatExport.TextEntity();
        plain.setType("plain");
        plain.setText("@notamention");

        ChatExport.TextEntity mention = new ChatExport.TextEntity();
        mention.setType("mention");
        mention.setText("@username");

        ChatExport.Message message = new ChatExport.Message();
        message.setFrom("Пользователь");
        message.setFromId("user111");
        message.setTextEntities(List.of(plain, mention));

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(message));

        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getMentionsCount());
        assertTrue(result.mentions().contains(new Mention("@username")));
    }

    @Test
    void throwExceptionOnNullChatExport() {
        assertThrows(ChatMessageAnalyzer.ChatAnalysisException.class, () -> analyzer.analyze(null));
    }

    private ChatExport createTestChatExport() {
        ChatExport chatExport = new ChatExport();
        chatExport.setName("Тест");
        chatExport.setType("personal_chat");

        ChatExport.Message message1 = new ChatExport.Message();
        message1.setFrom("Сергей Киевский");
        message1.setFromId("user123456789");
        ChatExport.TextEntity entity1 = new ChatExport.TextEntity();
        entity1.setType("plain");
        entity1.setText("first message");
        message1.setTextEntities(List.of(entity1));

        ChatExport.Message message2 = new ChatExport.Message();
        message2.setFrom("Sergey Kievskiy");
        message2.setFromId("user123123123");
        ChatExport.TextEntity entity2 = new ChatExport.TextEntity();
        entity2.setType("plain");
        entity2.setText("second message");
        message2.setTextEntities(List.of(entity2));

        ChatExport.Message message3 = new ChatExport.Message();
        message3.setFrom("Сергей Киевский");
        message3.setFromId("user123456789");
        ChatExport.TextEntity entity3a = new ChatExport.TextEntity();
        entity3a.setType("plain");
        entity3a.setText("message with ");
        ChatExport.TextEntity entity3b = new ChatExport.TextEntity();
        entity3b.setType("mention");
        entity3b.setText("@skievskiy");
        message3.setTextEntities(List.of(entity3a, entity3b));

        ChatExport.Message message4 = new ChatExport.Message();
        message4.setFrom("Сергей Киевский");
        message4.setFromId("user123456789");
        ChatExport.TextEntity entity4a = new ChatExport.TextEntity();
        entity4a.setType("mention");
        entity4a.setText("@skievskiy");
        ChatExport.TextEntity entity4b = new ChatExport.TextEntity();
        entity4b.setType("mention");
        entity4b.setText("@skievskiywork");
        message4.setTextEntities(List.of(entity4a, entity4b));

        chatExport.setMessages(List.of(message1, message2, message3, message4));
        return chatExport;
    }
}
