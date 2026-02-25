package ru.kievsan.chuserbot.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Модель экспорта чата.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatExport {

    private String name;
    private String type;
    private Long id;
    private List<Message> messages;

    /**
     * Сообщение в чате.
     * Поле text из JSON не парсится, т.к. может быть строкой или массивом.
     * Используется только textEntities для извлечения упоминаний.
     */
    @Data
    public static class Message {
        private String from;

        @JsonProperty("from_id")
        private String fromId;

        @JsonProperty("text_entities")
        private List<TextEntity> textEntities;

        /**
         * Получить текст сообщения из textEntities.
         * Используется для удобства, основная логика работы с textEntities.
         */
        public String getText() {
            if (textEntities == null) {
                return "";
            }
            return textEntities.stream()
                    .filter(Objects::nonNull)
                    .map(TextEntity::getText)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining());
        }
    }

    /**
     * Текстовая сущность в сообщении (plain, mention и т.д.).
     */
    @Data
    public static class TextEntity {
        private String type;
        private String text;
    }
}
