package ru.kievsan.chuserbot.export;

import ru.kievsan.chuserbot.domain.ChatMsgAnalysisResult;
import ru.kievsan.chuserbot.domain.ReportResult;

/**
 * Интерфейс для форматирования результата анализа.
 */
public interface ReportRenderer {

    /**
     * Отформатировать результат анализа: создаёт текстовый ответ или Excel-файл в зависимости от количества сущностей.
     *
     * @param analysisResult результат анализа чата.
     * @param fileName       имя исходного файла экспорта.
     * @return результат форматирования (ReportTextResult или ReportExcelResult).
     * @throws ReportRenderException если не удалось сформировать результат.
     */
    ReportResult render(ChatMsgAnalysisResult analysisResult, String fileName) throws ReportRenderException;

    /**
     * Исключение при форматировании отчета.
     */
    class ReportRenderException extends Exception {
        public ReportRenderException(String message) {
            super(message);
        }

        public ReportRenderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
