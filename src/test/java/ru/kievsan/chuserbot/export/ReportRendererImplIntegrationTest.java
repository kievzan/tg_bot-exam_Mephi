package ru.kievsan.chuserbot.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import ru.kievsan.chuserbot.analytics.ChatMessageAnalyzerImpl;
import ru.kievsan.chuserbot.config.BotConfig;
import ru.kievsan.chuserbot.domain.*;
import ru.kievsan.chuserbot.domain.ReportExcelExportResult;
import ru.kievsan.chuserbot.parser.ParserImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ReportRendererImplIntegrationTest {

    private final ParserImpl parser = new ParserImpl();
    private final ChatMessageAnalyzerImpl analyzer = new ChatMessageAnalyzerImpl();
    private final ReportRendererImpl renderer = new ReportRendererImpl();

    private String readResourceAsString(String resourceName) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(stream, "Resource not found: " + resourceName);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private Set<Member> createMembers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new Member("user" + i, "Имя " + i))
                .collect(java.util.stream.Collectors.toSet());
    }

    private Set<Mention> createMentions(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new Mention("@username" + i))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void renderTextForRealChatExport() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile rawFile = new RawChatFile("chat1.json", jsonContent);

        ChatExport chatExport = parser.parse(rawFile);
        ChatMsgAnalysisResult analysisResult = analyzer.analyze(chatExport);
        ReportExportResult reportExportResult = renderer.render(analysisResult, "chat1.json");

        // текстовый ответ (totalCount = 4 < 50)
        assertInstanceOf(ReportTextExportResult.class, reportExportResult);
        ReportTextExportResult textResult = (ReportTextExportResult) reportExportResult;
        assertEquals("chat1.json", textResult.fileName());
        assertNotNull(textResult.text());

        String text = textResult.text();

        // заголовок с количеством
        assertTrue(text.contains("Количество участников: 2"));
        assertTrue(text.contains("Количество упоминаний: 2"));

        // участников
        assertTrue(text.contains("Участники:"));
        assertTrue(text.contains("Владислав Почернин"));
        assertTrue(text.contains("Егор Мартынов"));

        // упоминания
        assertTrue(text.contains("Упоминания:"));
        assertTrue(text.contains("@vspochernin"));
        assertTrue(text.contains("@vspocherninwork"));
    }

    @Test
    void renderExcelForLargeChatExport() throws Exception {
        // Создаем большой набор данных, чтобы превысить порог.
        ChatMsgAnalysisResult analysisResult = new ChatMsgAnalysisResult(
                createMembers(BotConfig.EXCEL_THRESHOLD / 2 + 1),
                createMentions(BotConfig.EXCEL_THRESHOLD / 2 + 1)
        );

        ReportExportResult reportRes = renderer.render(analysisResult, "chat1.json");

        // Excel (totalCount >= 51)
        assertInstanceOf(ReportExcelExportResult.class, reportRes);
        ReportExcelExportResult excelResult = (ReportExcelExportResult) reportRes;
        assertEquals("chat1.json", excelResult.fileName());
        assertNotNull(excelResult.excelBytes());
        assertNotNull(excelResult.excelFileName());
        assertTrue(excelResult.excelFileName().endsWith(".xlsx"));

        // структуру Excel-файла
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelResult.excelBytes()))) {
            assertEquals(2, workbook.getNumberOfSheets());

            // лист Members
            Sheet participantsSheet = workbook.getSheetAt(0);
            assertEquals("Участники", participantsSheet.getSheetName());
            Row headerRow = participantsSheet.getRow(0);
            assertEquals("Дата экспорта", headerRow.getCell(0).getStringCellValue());
            assertEquals("UserId", headerRow.getCell(1).getStringCellValue());
            assertEquals("Имя и фамилия", headerRow.getCell(2).getStringCellValue());
            assertTrue(participantsSheet.getLastRowNum() >= BotConfig.EXCEL_THRESHOLD / 2);

            // лист Mentions
            Sheet mentionsSheet = workbook.getSheetAt(1);
            assertEquals("Упоминания", mentionsSheet.getSheetName());
            Row mentionsHeaderRow = mentionsSheet.getRow(0);
            assertEquals("Дата экспорта", mentionsHeaderRow.getCell(0).getStringCellValue());
            assertEquals("Username", mentionsHeaderRow.getCell(1).getStringCellValue());
            assertEquals(2, mentionsHeaderRow.getLastCellNum());
            assertTrue(mentionsSheet.getLastRowNum() >= BotConfig.EXCEL_THRESHOLD / 2);
        }
    }

    @Test
    void renderTextWhenExactlyOneLessThanThreshold() throws Exception {
        // результат с totalCount = 50
        ChatMsgAnalysisResult analysisResult = new ChatMsgAnalysisResult(
                createMembers(BotConfig.EXCEL_THRESHOLD - 1),
                Set.of()
        );

        ReportExportResult reportExportResult = renderer.render(analysisResult, "chat1.json");

        assertInstanceOf(ReportTextExportResult.class, reportExportResult);
        ReportTextExportResult textResult = (ReportTextExportResult) reportExportResult;
        assertNotNull(textResult.text());
    }

    @Test
    void renderExcelWhenExactlyAtThreshold() throws Exception {
        // Создаем результат с totalCount = 51 (порог).
        ChatMsgAnalysisResult analysisResult = new ChatMsgAnalysisResult(
                createMembers(BotConfig.EXCEL_THRESHOLD),
                Set.of()
        );

        ReportExportResult reportExportResult = renderer.render(analysisResult, "chat1.json");

        assertInstanceOf(ReportExcelExportResult.class, reportExportResult);
        ReportExcelExportResult excelResult = (ReportExcelExportResult) reportExportResult;
        assertNotNull(excelResult.excelBytes());
    }
}
