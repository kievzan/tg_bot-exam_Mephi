package ru.kievsan.chuserbot.domain;

/**
 * Результат форматирования отчета в виде текста.
 */
public final class ReportTextExportResult extends ReportExportResult {

    private final String text;

    public ReportTextExportResult(String fileName, String text) {
        super(fileName);
        if (text == null) {
            throw new IllegalArgumentException("'text' cannot be null");
        }
        this.text = text;
    }

    /**
     * Текстовое содержимое отчета.
     */
    public String text() {
        return text;
    }
}

