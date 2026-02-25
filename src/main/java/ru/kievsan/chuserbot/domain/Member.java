package ru.kievsan.chuserbot.domain;

import java.util.Objects;

/**
 * Участник чата (автор сообщений).
 */
public record Member(String fromId, String displayName) {

    public Member {
        if (fromId == null || fromId.isBlank()) {
            throw new IllegalArgumentException("fromId cannot be null or blank");
        }
        if (displayName == null) {
            throw new IllegalArgumentException("displayName cannot be null");
        }
        // displayName может быть пустой строкой (blank), кажется, в Telegram можно указать пустое имя.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member that = (Member) o;
        return Objects.equals(fromId, that.fromId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromId);
    }
}
