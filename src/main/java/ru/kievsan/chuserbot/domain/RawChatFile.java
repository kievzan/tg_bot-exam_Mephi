package ru.kievsan.chuserbot.domain;

/**
 * Представление сырого файла экспорта чата в виде строки JSON.
 * Используется для передачи между слоями до парсинга.
 */
public record RawChatFile(String fileName, String jsonContent) {

    public RawChatFile {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName cannot be null or blank");
        }
        if (jsonContent == null) {
            throw new IllegalArgumentException("jsonContent cannot be null");
        }
    }
}
