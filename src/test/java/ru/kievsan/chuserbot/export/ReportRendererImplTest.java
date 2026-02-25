package ru.kievsan.chuserbot.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import ru.kievsan.chuserbot.config.BotConfig;
import ru.kievsan.chuserbot.domain.*;
import ru.kievsan.chuserbot.domain.Member;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReportRendererImplTest {

    private final ReportRendererImpl renderer = new ReportRendererImpl();

    private ChatMsgAnalysisResult createLargeResult(int totalCount) {
        int half = totalCount / 2;
        return new ChatMsgAnalysisResult(
                createMembers(half),
                createMentions(totalCount - half)
        );
    }

    private Set<Member> createMembers(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new Member("user" + i, "Имя " + i))
                .collect(java.util.stream.Collectors.toSet());
    }

    private Set<Mention> createMentions(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new Mention("@username" + i))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void returnTextWhenTotalCountLessThanThreshold() throws Exception {
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                Set.of(new Member("user1", "Иван Иванов")),
                Set.of(new Mention("@username1"))
        );

        ReportResult reportResult = renderer.render(result, "test.json");

        assertInstanceOf(ReportTextResult.class, reportResult);
        ReportTextResult textResult = (ReportTextResult) reportResult;
        assertEquals("test.json", textResult.fileName());
        assertNotNull(textResult.text());
        assertTrue(textResult.text().contains("Файл: test.json"));
        assertTrue(textResult.text().contains("Количество участников: 1"));
    }

    @Test
    void returnExcelWhenTotalCountGreaterOrEqualThanThreshold() throws Exception {
        ChatMsgAnalysisResult result = createLargeResult(BotConfig.EXCEL_THRESHOLD);

        ReportResult reportResult = renderer.render(result, "test.json");

        assertInstanceOf(ReportExcelResult.class, reportResult);
        ReportExcelResult excelResult = (ReportExcelResult) reportResult;
        assertEquals("test.json", excelResult.fileName());
        assertNotNull(excelResult.excelBytes());
        assertNotNull(excelResult.excelFileName());
        assertTrue(excelResult.excelFileName().endsWith(".xlsx"));
    }

    @Test
    void returnExcelWhenTotalCountEqualsThreshold() throws Exception {
        ChatMsgAnalysisResult result = createLargeResult(BotConfig.EXCEL_THRESHOLD);

        ReportResult reportResult = renderer.render(result, "test.json");

        assertInstanceOf(ReportExcelResult.class, reportResult);
    }

    @Test
    void returnTextWhenTotalCountEqualsThresholdMinusOne() throws Exception {
        ChatMsgAnalysisResult result = createLargeResult(BotConfig.EXCEL_THRESHOLD - 1);

        ReportResult reportResult = renderer.render(result, "test.json");

        assertInstanceOf(ReportTextResult.class, reportResult);
    }

    @Test
    void shouldFormatTextResultCorrectly() throws Exception {
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                Set.of(
                        new Member("user1", "Иван Иванов"),
                        new Member("user2", "Петр Петров")
                ),
                Set.of(
                        new Mention("@username1"),
                        new Mention("@username2")
                )
        );

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportTextResult.class, reportResult);
        ReportTextResult textResult = (ReportTextResult) reportResult;
        String text = textResult.text();

        assertTrue(text.contains("Количество участников: 2"));
        assertTrue(text.contains("Количество упоминаний: 2"));
        assertTrue(text.contains("Участники:"));
        assertTrue(text.contains("Иван Иванов"));
        assertTrue(text.contains("Петр Петров"));
        assertTrue(text.contains("Упоминания:"));
        assertTrue(text.contains("@username1"));
        assertTrue(text.contains("@username2"));
    }

    @Test
    void formatTextResultWithBlankDisplayName() throws Exception {
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                Set.of(new Member("user1", "")),
                Set.of()
        );

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportTextResult.class, reportResult);
        ReportTextResult textResult = (ReportTextResult) reportResult;
        String text = textResult.text();

        assertTrue(text.contains("Участники:"));
        assertTrue(text.contains("- \n")); // Пустое имя.
    }

    @Test
    void formatTextResultWithoutMentionsSectionWhenEmpty() throws Exception {
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                Set.of(new Member("user1", "Иван Иванов")),
                Set.of()
        );

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportTextResult.class, reportResult);
        ReportTextResult textResult = (ReportTextResult) reportResult;
        String text = textResult.text();

        assertTrue(text.contains("Участники:"));
        assertFalse(text.contains("Упоминания:")); // Не должно быть секции упоминаний.
    }

    @Test
    void createExcelWithCorrectSheetNames() throws Exception {
        ChatMsgAnalysisResult result = createLargeResult(BotConfig.EXCEL_THRESHOLD);

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportExcelResult.class, reportResult);
        ReportExcelResult excelResult = (ReportExcelResult) reportResult;

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelResult.excelBytes()))) {
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("Участники", workbook.getSheetAt(0).getSheetName());
            assertEquals("Упоминания", workbook.getSheetAt(1).getSheetName());
        }
    }

    @Test
    void createExcelWithCorrectParticipantsHeaders() throws Exception {
        ChatMsgAnalysisResult result = createLargeResult(BotConfig.EXCEL_THRESHOLD);

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportExcelResult.class, reportResult);
        ReportExcelResult excelResult = (ReportExcelResult) reportResult;

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelResult.excelBytes()))) {
            Sheet participantsSheet = workbook.getSheetAt(0);
            Row headerRow = participantsSheet.getRow(0);

            assertEquals("Дата экспорта", headerRow.getCell(0).getStringCellValue());
            assertEquals("UserId", headerRow.getCell(1).getStringCellValue());
            assertEquals("Имя и фамилия", headerRow.getCell(2).getStringCellValue());
        }
    }

    @Test
    void createExcelWithCorrectMentionsHeaders() throws Exception {
        ChatMsgAnalysisResult result = createLargeResult(BotConfig.EXCEL_THRESHOLD);

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportExcelResult.class, reportResult);
        ReportExcelResult excelResult = (ReportExcelResult) reportResult;

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelResult.excelBytes()))) {
            Sheet mentionsSheet = workbook.getSheetAt(1);
            Row headerRow = mentionsSheet.getRow(0);

            assertEquals("Дата экспорта", headerRow.getCell(0).getStringCellValue());
            assertEquals("Username", headerRow.getCell(1).getStringCellValue());
            assertEquals(2, headerRow.getLastCellNum()); // 2 колонки: Дата экспорта, Username.
        }
    }

    @Test
    void createExcelWithCorrectParticipantsData() throws Exception {
        LocalDate today = LocalDate.now();
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                Set.of(
                        new Member("user123", "Иван Иванов"),
                        new Member("user456", "Петр Петров")
                ),
                createMentions(BotConfig.EXCEL_THRESHOLD - 2)
        );

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportExcelResult.class, reportResult);
        ReportExcelResult excelResult = (ReportExcelResult) reportResult;

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelResult.excelBytes()))) {
            Sheet participantsSheet = workbook.getSheetAt(0);

            assertEquals(3, participantsSheet.getLastRowNum() + 1); // Header + 2 rows.
            Row row1 = participantsSheet.getRow(1);
            Row row2 = participantsSheet.getRow(2);

            // Проверяем, что обе строки содержат правильные данные (порядок может быть любым).
            java.util.Set<String> userIds = new java.util.HashSet<>();
            java.util.Set<String> names = new java.util.HashSet<>();
            userIds.add(row1.getCell(1).getStringCellValue());
            userIds.add(row2.getCell(1).getStringCellValue());
            names.add(row1.getCell(2).getStringCellValue());
            names.add(row2.getCell(2).getStringCellValue());

            assertEquals(today.toString(), row1.getCell(0).getStringCellValue());
            assertEquals(today.toString(), row2.getCell(0).getStringCellValue());
            assertTrue(userIds.contains("user123"));
            assertTrue(userIds.contains("user456"));
            assertTrue(names.contains("Иван Иванов"));
            assertTrue(names.contains("Петр Петров"));
        }
    }

    @Test
    void createExcelWithCorrectMentionsData() throws Exception {
        LocalDate today = LocalDate.now();
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                createMembers(BotConfig.EXCEL_THRESHOLD - 2),
                Set.of(
                        new Mention("@username1"),
                        new Mention("@username2")
                )
        );

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportExcelResult.class, reportResult);
        ReportExcelResult excelResult = (ReportExcelResult) reportResult;

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelResult.excelBytes()))) {
            Sheet mentionsSheet = workbook.getSheetAt(1);

            assertEquals(3, mentionsSheet.getLastRowNum() + 1); // Header + 2 rows.
            Row row1 = mentionsSheet.getRow(1);
            Row row2 = mentionsSheet.getRow(2);

            // Проверяем, что обе строки содержат правильные данные (порядок может быть любым).
            java.util.Set<String> mentions = new java.util.HashSet<>();
            mentions.add(row1.getCell(1).getStringCellValue());
            mentions.add(row2.getCell(1).getStringCellValue());

            assertEquals(today.toString(), row1.getCell(0).getStringCellValue());
            assertEquals(today.toString(), row2.getCell(0).getStringCellValue());
            assertEquals(2, row1.getLastCellNum()); // 2 колонки: Дата экспорта, Username.
            assertEquals(2, row2.getLastCellNum()); // 2 колонки: Дата экспорта, Username.
            assertTrue(mentions.contains("@username1"));
            assertTrue(mentions.contains("@username2"));
        }
    }

    @Test
    void handleEmptyMembers() throws Exception {
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                Set.of(),
                Set.of(new Mention("@username1"))
        );

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportTextResult.class, reportResult);
        ReportTextResult textResult = (ReportTextResult) reportResult;
        String text = textResult.text();
        assertTrue(text.contains("Количество участников: 0"));
        assertTrue(text.contains("Количество упоминаний: 1"));
    }

    @Test
    void handleEmptyMentions() throws Exception {
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                Set.of(new Member("user1", "Иван Иванов")),
                Set.of()
        );

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportTextResult.class, reportResult);
        ReportTextResult textResult = (ReportTextResult) reportResult;
        String text = textResult.text();
        assertTrue(text.contains("Количество участников: 1"));
        assertTrue(text.contains("Количество упоминаний: 0"));
    }

    @Test
    void throwExceptionOnNullAnalysisResult() {
        assertThrows(Exception.class, () -> renderer.render(null, "test.json"));
    }

    @Test
    void createExcelWithParticipantsOnly() throws Exception {
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                createMembers(BotConfig.EXCEL_THRESHOLD),
                Set.of()
        );

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportExcelResult.class, reportResult);
        ReportExcelResult excelResult = (ReportExcelResult) reportResult;

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelResult.excelBytes()))) {
            assertEquals(2, workbook.getNumberOfSheets());
            Sheet participantsSheet = workbook.getSheetAt(0);
            assertTrue(participantsSheet.getLastRowNum() >= BotConfig.EXCEL_THRESHOLD);
        }
    }

    @Test
    void createExcelWithMentionsOnly() throws Exception {
        ChatMsgAnalysisResult result = new ChatMsgAnalysisResult(
                Set.of(),
                createMentions(BotConfig.EXCEL_THRESHOLD)
        );

        ReportResult reportResult = renderer.render(result, "test.json");
        assertInstanceOf(ReportExcelResult.class, reportResult);
        ReportExcelResult excelResult = (ReportExcelResult) reportResult;

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelResult.excelBytes()))) {
            assertEquals(2, workbook.getNumberOfSheets());
            Sheet mentionsSheet = workbook.getSheetAt(1);
            assertTrue(mentionsSheet.getLastRowNum() >= BotConfig.EXCEL_THRESHOLD);
        }
    }
}
