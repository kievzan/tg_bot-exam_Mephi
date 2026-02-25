package ru.kievsan.chuserbot;

import org.junit.jupiter.api.Test;
import ru.kievsan.chuserbot.analytics.ChatMessageAnalyzerImpl;
import ru.kievsan.chuserbot.domain.*;
import ru.kievsan.chuserbot.domain.Member;
import ru.kievsan.chuserbot.parser.ParserImpl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ChatAnalyzerIntegrationTest {

    private String readResourceAsString(String resourceName) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(stream, "Resource not found: " + resourceName);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    void analyzeRealChatExport() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        var parser = new ParserImpl();
        var analyzer = new ChatMessageAnalyzerImpl();

        ChatExport chatExport = parser.parse(new RawChatFile("chat1.json", jsonContent));
        ChatMsgAnalysisResult result = analyzer.analyze(chatExport);

        // должны быть 2 уникальных участника (Сергей Киевский и Sergey Kievskiy).
        assertEquals(2, result.getMembersCount());
        assertTrue(result.members().contains(new Member("user123456789", "Сергей Киевский")));
        assertTrue(result.members().contains(new Member("user123123123", "Sergey Kievskiy")));

        // должны быть 2 уникальных упоминания (@vspochernin и @vspocherninwork).
        assertEquals(2, result.getMentionsCount());
        assertTrue(result.mentions().contains(new Mention("@skievskiy")));
        assertTrue(result.mentions().contains(new Mention("@skievskiywork")));

        // всего
        assertEquals(4, result.getTotalCount());
    }
}
