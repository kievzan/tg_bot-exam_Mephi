package ru.kievsan.chuserbot.tg;

import ru.kievsan.chuserbot.analytics.ChatMessageAnalyzer;
import ru.kievsan.chuserbot.domain.ChatMsgAnalysisResult;
import ru.kievsan.chuserbot.domain.ChatExport;
import ru.kievsan.chuserbot.domain.RawChatFile;
import ru.kievsan.chuserbot.domain.ReportResult;
import ru.kievsan.chuserbot.export.ReportRenderer;
import ru.kievsan.chuserbot.parser.Parser;

/**
 * Сервис обработки файла экспорта чата
 * ( парсер + анализатор + рендерер )
 *
 */
public class ChatProcService {

    private final Parser parser;
    private final ChatMessageAnalyzer analyzer;
    private final ReportRenderer renderer;

    public ChatProcService(
            Parser parser,
            ChatMessageAnalyzer analyzer,
            ReportRenderer renderer)
    {
        this.parser = parser;
        this.analyzer = analyzer;
        this.renderer = renderer;
    }

    /**
     * Обработать один файл экспорта чата: распарсить, проанализировать, отформатировать результат.
     *
     * @param file файл экспорта чата.
     * @return результат обработки в формате текста или Excel.
     * @throws ChatProcessingException если обработка не удалась.
     */
    public ReportResult process(RawChatFile file) throws ChatProcessingException {
        try {
            // 1. Парсим JSON в доменную модель.
            ChatExport chatExport = parser.parse(file);

            // 2. Анализируем и извлекаем участников/упоминания.
            ChatMsgAnalysisResult analysisResult = analyzer.analyze(chatExport);

            // 3. Форматируем результат (текст или Excel).
            return renderer.render(analysisResult, file.fileName());

        } catch (Parser.ChatExportParseException e) {
            throw new ChatProcessingException("Failed to parse chat export", e);
        } catch (ChatMessageAnalyzer.ChatAnalysisException e) {
            throw new ChatProcessingException("Failed to analyze chat export", e);
        } catch (ReportRenderer.ReportRenderException e) {
            throw new ChatProcessingException("Failed to render report", e);
        }
    }

    /**
     * Исключение при обработке файла.
     */
    public static class ChatProcessingException extends Exception {
        public ChatProcessingException(String message) {
            super(message);
        }

        public ChatProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
