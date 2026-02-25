package ru.kievsan.chuserbot;

import org.junit.jupiter.api.Test;
import ru.kievsan.chuserbot.domain.ChatExport;
import ru.kievsan.chuserbot.domain.RawChatFile;
import ru.kievsan.chuserbot.parser.Parser.ChatExportParseException;
import ru.kievsan.chuserbot.parser.ParserImpl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatExportParserImplTest {

    private String readResourceAsString(String resourceName) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(stream, "Not found: " + resourceName);
        assert stream != null;
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    void parseRealChatJson() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile file = new RawChatFile("chat1.json", jsonContent);
        ParserImpl parser = new ParserImpl();

        ChatExport result = parser.parse(file);

        assertNotNull(result);
        assertEquals("Sergey", result.getName());
        assertEquals("personal_chat", result.getType());
        assertEquals(123123123L, result.getId());

        List<ChatExport.Message> messages = result.getMessages();
        assertNotNull(messages);
        assertEquals(5, messages.size());
    }

    @Test
    void parseMessagesCorrectly() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile file = new RawChatFile("chat1.json", jsonContent);
        ParserImpl parser = new ParserImpl();

        ChatExport result = parser.parse(file);
        List<ChatExport.Message> messages = result.getMessages();

        ChatExport.Message firstMessage = messages.get(0);
        assertEquals("Сергей Киевский", firstMessage.getFrom());
        assertEquals("user123456789", firstMessage.getFromId());
        assertNotNull(firstMessage.getTextEntities());
        assertFalse(firstMessage.getTextEntities().isEmpty());
        assertEquals("first message", firstMessage.getText());
    }

    @Test
    void parseMentionsCorrectly() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile file = new RawChatFile("chat1.json", jsonContent);
        ParserImpl parser = new ParserImpl();

        ChatExport result = parser.parse(file);
        List<ChatExport.Message> messages = result.getMessages();

        // msg с упоминанием (4-е по счету, индекс 3).
        ChatExport.Message messageWithMention = messages.get(3);
        List<ChatExport.TextEntity> entities = messageWithMention.getTextEntities();

        assertTrue(entities.stream()
                .anyMatch(e -> "mention".equals(e.getType()) && "@skievskiy".equals(e.getText())));

        // есть и plain текст?
        assertTrue(entities.stream().anyMatch(e -> "plain".equals(e.getType())));
    }

    @Test
    void throwExceptionOnBlankContent() {
        RawChatFile file = new RawChatFile("empty.json", "");
        ParserImpl parser = new ParserImpl();

        assertThrows(ChatExportParseException.class, () -> parser.parse(file));
    }

    @Test
    void throwExceptionOnInvalidJson() {
        RawChatFile file = new RawChatFile("invalid.json", "{ invalid json }");
        ParserImpl parser = new ParserImpl();

        assertThrows(ChatExportParseException.class, () -> parser.parse(file));
    }

    @Test
    void parseMultipleMentionsInOneMessage() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile file = new RawChatFile("chat1.json", jsonContent);
        ParserImpl parser = new ParserImpl();

        ChatExport result = parser.parse(file);
        List<ChatExport.Message> messages = result.getMessages();

        // last msg с двумя упоминаниями (индекс 4).
        ChatExport.Message messageWithMultipleMentions = messages.get(4);
        List<ChatExport.TextEntity> entities = messageWithMultipleMentions.getTextEntities();

        long mentionCount = entities.stream()
                .filter(e -> "mention".equals(e.getType()))
                .count();

        assertEquals(2, mentionCount, "Should have 2 mentions");
        assertTrue(entities.stream()
                .anyMatch(e -> "mention".equals(e.getType()) && "@skievskiy".equals(e.getText())));
        assertTrue(entities.stream()
                .anyMatch(e -> "mention".equals(e.getType()) && "@skievskiywork".equals(e.getText())));
    }

    @Test
    void handleMessageWithEmptyTextEntities() throws Exception {
        String jsonWithEmptyEntities = """
                {
                  "name": "Test",
                  "type": "personal_chat",
                  "id": 123,
                  "messages": [
                    {
                      "from": "User",
                      "from_id": "user123",
                      "text_entities": []
                    }
                  ]
                }
                """;

        RawChatFile file = new RawChatFile("test.json", jsonWithEmptyEntities);
        ParserImpl parser = new ParserImpl();

        ChatExport result = parser.parse(file);
        assertNotNull(result);
        assertEquals(1, result.getMessages().size());

        ChatExport.Message message = result.getMessages().getFirst();
        assertNotNull(message.getTextEntities());
        assertTrue(message.getTextEntities().isEmpty());
        assertEquals("", message.getText());
    }
}
