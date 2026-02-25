package ru.kievsan.chuserbot.tg;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.kievsan.chuserbot.domain.RawChatFile;
import ru.kievsan.chuserbot.domain.ReportExcelResult;
import ru.kievsan.chuserbot.domain.ReportResult;
import ru.kievsan.chuserbot.domain.ReportTextResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * Базовый класс Telegram-бота.
 */
@Slf4j
public class ChuserBot implements LongPollingSingleThreadUpdateConsumer {

    private static final String COMMAND_START = "/start";
    private static final String COMMAND_HELP = "/help";
    private static final String API_TELEGRAM_FILE_BOT_BASE_URL = "https://api.telegram.org/file/bot";

    private final TelegramClient tgClient;
    private final String botToken;
    private final ChatProcService procService;

    public ChuserBot(String botToken, ChatProcService procService) {
        log.info("ChuserBot instance creating...");
        this.tgClient = new OkHttpTelegramClient(botToken);
        this.botToken = botToken;
        this.procService = procService;
        log.info("OK");
    }

    @Override
    public void consume(Update update) {
        if (update == null) {
            log.warn("Please, ignore received null update!");
            return;
        }

        if (!update.hasMessage()) {
            log.warn("Please, ignore update without message: {}", update);
            return;
        }

        Message updateMsg = update.getMessage();
        Long chatId = updateMsg.getChatId();

        try {
            if (updateMsg.hasText() && updateMsg.getText().startsWith("/")) {
                handleCommand(chatId, updateMsg.getText());
            } else if (updateMsg.hasDocument()) {
                handleDocMsg(chatId, updateMsg.getDocument());
            } else if (updateMsg.hasText()) {
                handlePlainTextMsg(chatId);
            } else {
                log.warn("Unsupported message type in chat {}: {}", chatId, updateMsg);
                sendText(chatId, "Могу обрабатывать только текст, файлы или команды...");
            }
        } catch (Exception e) {
            log.error("Update error in chat {}", chatId, e);
            sendText(chatId, "Unexpected error, please try again later...");
        }
    }

    private void sendText(Long chatId, String text) {
        if (chatId == null) {
            log.warn("Попытка отправить сообщение с нулевым id чата, пропустить!");
            return;
        }

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();

        try {
            tgClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в чат {}", chatId, e);
        }
    }

    private void sendStartMsg(Long chatId) {
        String msg = """
                Hi! Я Chuser Bot.
                
                Мне необходим(ы) JSON-файл(ы) экспорта чата из Telegram Desktop.
                Я проанализирую их и подготовлю список участников / Excel-файл согласно заданию.
                
                Для справки используйте команду /help.
                """.strip();
        sendText(chatId, msg);
    }

    private void sendHelpMsg(Long chatId) {
        String msg = """
                Hi! Я Chuser Bot. Я умею:
                
                - Принимаю JSON-экспорт истории чата (Telegram Desktop -> Export chat history -> JSON).
                - Каждый файл обрабатывается сразу после отправки.
                - Извлекаю участников (авторов сообщений) и упоминания (@username).
                - Если всего сущностей <= 50 - отправляю список прямо в чат.
                - Если всего сущностей >= 51 - формирую и отправляю Excel-файл.
                
                Пожалуйста, отправьте мне .json-файл экспорта чата.
                """.strip();
        sendText(chatId, msg);
    }

    private void handleCommand(Long chatId, String text) {
        String command = text.split("\\s+", 2)[0];
        switch (command) {
            case COMMAND_START -> sendStartMsg(chatId);
            case COMMAND_HELP -> sendHelpMsg(chatId);
            default -> sendText(chatId, "Неизвестная команда. Выполните '/start' или '/help'");
        }
    }

    private void handlePlainTextMsg(Long chatId) {
        String msg = """
                Hi! Мне нужны JSON-файлы экспорта чата.
                
                1) В Telegram Desktop сделайте экспорт истории чата в формате JSON.
                2) Загрузите полученный .json-файл, я обработаю его и верну результат.
                
                Если остались вопросы - наберите '/help'.
                """.strip();
        sendText(chatId, msg);
    }

    private void handleDocMsg(Long chatId, Document doc) {
        String fileName = doc.getFileName();
        String mimeType = doc.getMimeType();
        log.info(
                "Received document from chat {}: name='{}', mime='{}', size={}",
                chatId,
                fileName,
                mimeType,
                doc.getFileSize());

        if (fileName == null) {
            fileName = "unknown.json";
        }

        if (!fileName.toLowerCase().endsWith(".json")) {
            sendText(chatId, "Принимаются только файлы экспорта чата с расширением '.json'. " +
                    "Убедитесь, что подгружаем именно такие файлы из истории чата Telegram Desktop в формате JSON!");
            return;
        }

        String fileId = doc.getFileId();

        try (InputStream inputStream = downloadFileByStream(fileId)) {
            // in-memory
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            inputStream.transferTo(buffer);

            // Преобразуем в JSON (UTF-8).
            String jsonContent = buffer.toString(java.nio.charset.StandardCharsets.UTF_8);
            RawChatFile rawFile = new RawChatFile(fileName, jsonContent);
            sendText(chatId, "Обработка файла \"" + fileName + "\"...");

            ReportResult result = procService.process(rawFile);

            // Отправляем результат
            switch (result) {
                case ReportTextResult txtResult -> sendTextResult(chatId, tчtResult.text());
                case ReportExcelResult excelResult ->
                        sendExcelResult(chatId, excelResult.excelBytes(), excelResult.excelFileName());
                default -> {
                    log.error("Unknown ReportResult type: {}", result.getClass());
                    sendText(chatId, "Error forming the result!");
                }
            }

            log.info("File {} processed successfully for chat {}", fileName, chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to download file from Telegram for chat {}, fileId {}", chatId, fileId, e);
            sendText(chatId, "Couldn't download the file \"" + fileName + "\".");
        } catch (IOException e) {
            log.error("IO error while downloading file for chat {}, fileId {}", chatId, fileId, e);
            sendText(chatId, "File reading error \"" + fileName + "\".");
        } catch (ChatProcService.ChatProcessingException e) {
            log.error("Failed to process file {} for chat {}", fileName, chatId, e);
            sendText(chatId, "File processing error \"" + fileName + "\".");
        } catch (Exception e) {
            log.error("Unexpected error while processing file for chat {}, fileId {}", chatId, fileId, e);
            sendText(chatId, "Unexpected error during file processing \"" + fileName + "\".");
        }
    }

    private void sendTextResult(Long chatId, String text) {
        if (text == null || text.isBlank()) {
            log.error("Text is null or blank for chatId {}", chatId);
            sendText(chatId, "Ошибка: отсутствует результат обработки!");
            return;
        }

        // Telegram ограничивает длину сообщения 4096 символами.
        if (text.length() <= 4096) {
            sendText(chatId, text);
        } else {
            // Разбиваем на части.
            int offset = 0;
            while (offset < text.length()) {
                int endIndex = Math.min(offset + 4000, text.length());
                String chunk = text.substring(offset, endIndex);
                sendText(chatId, chunk);
                offset = endIndex;
            }
        }
    }

    private void sendExcelResult(Long chatId, byte[] excelBytes, String fileName) {
        if (excelBytes == null || excelBytes.length == 0) {
            log.error("Excel bytes is null or blank for chatId {}", chatId);
            sendText(chatId, "Ошибка: Excel-файл пуст.");
            return;
        }

        try {
            String excelFileName = fileName != null && !fileName.isBlank()
                    ? fileName
                    : "chuserbot_report.xlsx";

            InputFile inputFile = new InputFile(new ByteArrayInputStream(excelBytes), excelFileName);
            SendDocument sendDocument = SendDocument.builder()
                    .chatId(String.valueOf(chatId))
                    .document(inputFile)
                    .caption("Отчет 'Участники чата'")
                    .build();

            tgClient.execute(sendDocument);
            log.info("Excel file sent to chat {}", chatId);

        } catch (TelegramApiException e) {
            log.error("Failed to send Excel file to chat {}", chatId, e);
            sendText(chatId, "Не удалось отправить Excel-файл.");
        }
    }

    private InputStream downloadFileByStream(String fileId) throws TelegramApiException, IOException {
        GetFile getFileMethod = new GetFile(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = tgClient.execute(getFileMethod);

        String filePath = file.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalStateException("Received empty filePath for fileId " + fileId);
        }

        String urlString = API_TELEGRAM_FILE_BOT_BASE_URL + botToken + "/" + filePath;
        log.info("Downloading file from Telegram: {}", urlString);

        URL url = URI.create(urlString).toURL();
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }
}
