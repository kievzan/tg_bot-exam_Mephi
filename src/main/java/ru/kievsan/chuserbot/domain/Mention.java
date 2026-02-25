package ru.kievsan.chuserbot.domain;

import java.util.Objects;

/**
 * Упоминание пользователя в сообщении (@username).
 */
public record Mention(String mentionText) {

    public Mention {
        if (mentionText == null || mentionText.isBlank()) {
            throw new IllegalArgumentException("mentionText cannot be null or blank");
        }
        if (!mentionText.startsWith("@")) {
            throw new IllegalArgumentException("mentionText must start with @");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mention mention = (Mention) o;
        return Objects.equals(mentionText, mention.mentionText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mentionText);
    }
}
