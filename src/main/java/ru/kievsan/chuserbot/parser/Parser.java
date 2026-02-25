package ru.kievsan.chuserbot.parser;

import ru.kievsan.chuserbot.domain.ChatExport;
import ru.kievsan.chuserbot.domain.RawChatFile;

/**
 * Интерфейс для парсинга JSON-экспорта чата Telegram в доменную модель.
 */
public interface Parser {

    /**
     * Распарсить JSON-файл экспорта чата в доменную модель.
     *
     * @param file сырой файл экспорта чата.
     * @return объект доменной модели ChatExport.
     * @throws ChatExportParseException если файл не получается распарсить.
     */
    ChatExport parse(RawChatFile file) throws ChatExportParseException;

    /**
     * Исключение при парсинге JSON-экспорта.
     */
    class ChatExportParseException extends Exception {
        public ChatExportParseException(String message) {
            super(message);
        }

        public ChatExportParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
