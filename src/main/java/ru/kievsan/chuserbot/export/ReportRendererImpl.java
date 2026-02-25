package ru.kievsan.chuserbot.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.kievsan.chuserbot.config.BotConfig;
import ru.kievsan.chuserbot.domain.ChatMsgAnalysisResult;
import ru.kievsan.chuserbot.domain.Mention;
import ru.kievsan.chuserbot.domain.Member;
import ru.kievsan.chuserbot.domain.ReportExcelResult;
import ru.kievsan.chuserbot.domain.ReportResult;
import ru.kievsan.chuserbot.domain.ReportTextResult;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса для форматирования результата анализа.
 * Генерирует текстовый ответ или Excel-файл в зависимости от количества сущностей.
 */
public class ReportRendererImpl implements ReportRenderer {

    @Override
    public ReportResult render(ChatMsgAnalysisResult analysisResult, String fileName) throws ReportRenderException {
        try {
            int totalCount = analysisResult.getTotalCount();

            if (totalCount < BotConfig.EXCEL_THRESHOLD) {
                // Генерируем текстовый ответ.
                String text = renderText(analysisResult, fileName);
                return new ReportTextResult(fileName, text);
            } else {
                // Генерируем Excel-файл.
                byte[] excelBytes = renderExcel(analysisResult);
                String excelFileName = generateExcelFileName(fileName);
                return new ReportExcelResult(fileName, excelBytes, excelFileName);
            }
        } catch (Exception e) {
            throw new ReportRenderException("Failed to render report", e);
        }
    }

    private String renderText(ChatMsgAnalysisResult result, String fileName) {
        StringBuilder sb = new StringBuilder();
        int participantsCount = result.getMembersCount();
        int mentionsCount = result.getMentionsCount();

        sb.append("Файл: ").append(fileName).append("\n");
        sb.append("Количество участников: ").append(participantsCount).append("\n");
        sb.append("Количество упоминаний: ").append(mentionsCount).append("\n\n");

        sb.append("Участники:\n");
        result.members().forEach(p -> sb
                .append("- ")
                .append(p.displayName())
                .append("\n"));

        if (mentionsCount > 0) {
            sb.append("\nУпоминания:\n");
            result.mentions().forEach(m -> sb
                    .append("- ")
                    .append(m.mentionText())
                    .append("\n"));
        }

        return sb.toString();
    }

    private byte[] renderExcel(ChatMsgAnalysisResult result) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheetMembers = workbook.createSheet("Участники");
            Sheet sheetMentions = workbook.createSheet("Упоминания");

            createHeaderMembersSheet(sheetMembers);
            createHeaderMentionsSheet(sheetMentions);

            List<RowData> rowsMembers = collectRowsMembers(result);
            writeRowsMembers(sheetMembers, rowsMembers);

            List<RowData> rowsMentions = collectRowsMentions(result);
            writeRowsMentions(sheetMentions, rowsMentions);

            autosize(sheetMembers, 3);
            autosize(sheetMentions, 2);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private String generateExcelFileName(String fileName) {
        String baseFileName = fileName != null && !fileName.isBlank()
                ? sanitizeFileName(fileName.replace(".json", ""))
                : "chat-export";
        return baseFileName + "-" + LocalDate.now() + ".xlsx";
    }

    private void createHeaderMembersSheet(Sheet sheet) {
        Row header = sheet.createRow(0);

        String[] columns = {
                "Дата экспорта",
                "UserId",
                "Имя и фамилия",
        };

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }
    }

    private void createHeaderMentionsSheet(Sheet sheet) {
        Row header = sheet.createRow(0);

        String[] columns = {
                "Дата экспорта",
                "Username"
        };

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }
    }

    private List<RowData> collectRowsMembers(ChatMsgAnalysisResult result) {
        List<RowData> rows = new ArrayList<>();
        LocalDate exportDate = LocalDate.now();

        // Участники.
        for (Member p : result.members()) {
            rows.add(new RowData(exportDate.toString(), p.fromId(), p.displayName()));
        }

        return rows;
    }

    private List<RowData> collectRowsMentions(ChatMsgAnalysisResult result) {
        List<RowData> rows = new ArrayList<>();
        LocalDate exportDate = LocalDate.now();

        // Упоминания.
        for (Mention m : result.mentions()) {
            rows.add(new RowData(exportDate.toString(), m.mentionText(), ""));
        }

        return rows;
    }

    private void writeRowsMembers(Sheet sheet, List<RowData> rows) {
        int rowIndex = 1;
        for (RowData data : rows) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(data.exportDate);
            row.createCell(1).setCellValue(data.username);
            row.createCell(2).setCellValue(data.fullName);
        }
    }

    private void writeRowsMentions(Sheet sheet, List<RowData> rows) {
        int rowIndex = 1;
        for (RowData data : rows) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(data.exportDate);
            row.createCell(1).setCellValue(data.username);
        }
    }

    private void autosize(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "chat-export";
        }
        // Удаляем недопустимые символы для имени файла.
        return fileName.replaceAll("[^a-zA-Z0-9а-яА-ЯёЁ_\\-\\s]", "_")
                .replaceAll("\\s+", "_")
                .trim();
    }

    private record RowData(String exportDate, String username, String fullName) {
    }
}
