package ru.kievsan.chuserbot.domain;

/**
 * Абстрактный базовый класс для результата форматирования отчета.
 * Хранит имя исходного файла экспорта.
 */
public abstract class ReportResult {
    private final String fileName;

    protected ReportResult(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName cannot be null");
        }
        this.fileName = fileName;
    }

    /**
     * Получить имя исходного файла экспорта.
     */
    public String fileName() {
        return fileName;
    }
}
