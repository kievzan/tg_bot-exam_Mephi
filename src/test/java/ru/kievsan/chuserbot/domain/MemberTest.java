package ru.kievsan.chuserbot.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    void createMemberWithBlankDisplayName() {
        Member member = new Member("user123", "");
        assertEquals("user123", member.fromId());
        assertEquals("", member.displayName());
    }

    @Test
    void createMemberWithWhitespaceDisplayName() {
        Member member = new Member("user123", "   ");
        assertEquals("user123", member.fromId());
        assertEquals("   ", member.displayName());
    }

    @Test
    void throwExceptionOnNullDisplayName() {
        assertThrows(IllegalArgumentException.class, () -> new Member("user123", null));
    }

    @Test
    void throwExceptionOnNullFromId() {
        assertThrows(IllegalArgumentException.class, () -> new Member(null, "Display Name"));
    }

    @Test
    void throwExceptionOnBlankFromId() {
        assertThrows(IllegalArgumentException.class, () -> new Member("", "Display Name"));
    }

    @Test
    void equalByFromId() {
        Member member1 = new Member("user123", "Name 1");
        Member member2 = new Member("user123", "Name 2");
        Member member3 = new Member("user123", "");

        assertEquals(member1, member2);
        assertEquals(member1, member3);
        assertEquals(member1.hashCode(), member2.hashCode());
        assertEquals(member1.hashCode(), member3.hashCode());
    }

    @Test
    void notEqualByDifferentFromId() {
        Member member1 = new Member("user123", "Name");
        Member member2 = new Member("user456", "Name");

        assertNotEquals(member1, member2);
    }
}

