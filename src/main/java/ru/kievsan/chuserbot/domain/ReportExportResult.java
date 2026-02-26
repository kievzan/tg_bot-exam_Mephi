package ru.kievsan.chuserbot.domain;

/**
 * Абстрактный базовый класс для форматирования отчетов.
 * Хранит имя исх. файла экспорта.
 */
public abstract class ReportExportResult {
    private final String fileName;

    protected ReportExportResult(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("'fileName' cannot be null!");
        }
        this.fileName = fileName;
    }

    /**
     * Имя исх. файла экспорта.
     */
    public String fileName() {
        return fileName;
    }
}
