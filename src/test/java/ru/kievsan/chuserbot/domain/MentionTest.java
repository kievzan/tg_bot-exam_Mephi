package ru.kievsan.chuserbot.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MentionTest {

    @Test
    void createMentionWithValidText() {
        Mention mention = new Mention("@username");
        assertEquals("@username", mention.mentionText());
    }

    @Test
    void createMentionWithComplexText() {
        Mention mention = new Mention("@user_name_123");
        assertEquals("@user_name_123", mention.mentionText());
    }

    @Test
    void throwExceptionOnNullMentionText() {
        assertThrows(IllegalArgumentException.class, () -> new Mention(null));
    }

    @Test
    void throwExceptionOnBlankMentionText() {
        assertThrows(IllegalArgumentException.class, () -> new Mention(""));
    }

    @Test
    void throwExceptionOnWhitespaceMentionText() {
        assertThrows(IllegalArgumentException.class, () -> new Mention("   "));
    }

    @Test
    void throwExceptionIfNotStartsWithAt() {
        assertThrows(IllegalArgumentException.class, () -> new Mention("username"));
    }

    @Test
    void throwExceptionIfStartsWithSpaceThenAt() {
        assertThrows(IllegalArgumentException.class, () -> new Mention(" @username"));
    }

    @Test
    void equalByMentionText() {
        Mention mention1 = new Mention("@username");
        Mention mention2 = new Mention("@username");
        Mention mention3 = new Mention("@username");

        assertEquals(mention1, mention2);
        assertEquals(mention1, mention3);
        assertEquals(mention1.hashCode(), mention2.hashCode());
        assertEquals(mention1.hashCode(), mention3.hashCode());
    }

    @Test
    void notEqualByDifferentMentionText() {
        Mention mention1 = new Mention("@username1");
        Mention mention2 = new Mention("@username2");

        assertNotEquals(mention1, mention2);
        assertNotEquals(mention1.hashCode(), mention2.hashCode());
    }

    @Test
    void shouldBeCaseSensitive() {
        Mention mention1 = new Mention("@Username");
        Mention mention2 = new Mention("@username");

        assertNotEquals(mention1, mention2);
    }
}

