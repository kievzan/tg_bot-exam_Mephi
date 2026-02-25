package ru.kievsan.chuserbot.analytics;

import ru.kievsan.chuserbot.domain.ChatMsgAnalysisResult;
import ru.kievsan.chuserbot.domain.ChatExport;

/**
 * Интерфейс анализа экспорта чата и извлечения участников и упоминаний.
 */
public interface ChatMessageAnalyzer {

    /**
     * Анализ экспорта сообщений из чата с извлечением участников и упоминаний.
     *
     * @param chatExport объект доменной модели ChatExport.
     * @return результат анализа с уникальными участниками и упоминаниями.
     * @throws ChatAnalysisException когда анализ не удался.
     */
    ChatMsgAnalysisResult analyze(ChatExport chatExport) throws ChatAnalysisException;

    /**
     * Исключение в процессе анализа чата.
     */
    class ChatAnalysisException extends Exception {
        public ChatAnalysisException(String message) {
            super(message);
        }

        public ChatAnalysisException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
