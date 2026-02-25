package ru.kievsan.chuserbot.domain;

/**
 * Результат форматирования отчета в виде текста.
 */
public final class ReportTextResult extends ReportResult {

    private final String text;

    public ReportTextResult(String fileName, String text) {
        super(fileName);
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        this.text = text;
    }

    /**
     * Получить текстовое содержимое отчета.
     */
    public String text() {
        return text;
    }
}

