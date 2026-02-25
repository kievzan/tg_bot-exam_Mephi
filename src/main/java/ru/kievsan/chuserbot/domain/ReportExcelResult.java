package ru.kievsan.chuserbot.domain;

/**
 * Результат форматирования отчета в виде Excel-файла.
 */
public final class ReportExcelResult extends ReportResult {

    private final byte[] excelBytes;
    private final String excelFileName;

    public ReportExcelResult(String fileName, byte[] excelBytes, String excelFileName) {
        super(fileName);
        if (excelBytes == null) {
            throw new IllegalArgumentException("excelBytes cannot be null");
        }
        if (excelFileName == null) {
            throw new IllegalArgumentException("excelFileName cannot be null");
        }
        this.excelBytes = excelBytes;
        this.excelFileName = excelFileName;
    }

    /**
     * Получить содержимое Excel-файла в виде байтов.
     */
    public byte[] excelBytes() {
        return excelBytes;
    }

    /**
     * Получить имя Excel-файла для отправки.
     */
    public String excelFileName() {
        return excelFileName;
    }
}

