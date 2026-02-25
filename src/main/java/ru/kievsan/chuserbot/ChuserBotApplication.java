package ru.kievsan.chuserbot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kievsan.chuserbot.analytics.ChatMessageAnalyzer;
import ru.kievsan.chuserbot.analytics.ChatMessageAnalyzerImpl;
import ru.kievsan.chuserbot.export.ReportRenderer;
import ru.kievsan.chuserbot.export.ReportRendererImpl;
import ru.kievsan.chuserbot.parser.Parser;
import ru.kievsan.chuserbot.parser.ParserImpl;
import ru.kievsan.chuserbot.tg.ChuserBot;
import ru.kievsan.chuserbot.tg.ChatProcService;

@Slf4j
public class ChuserBotApplication {

    public static void main(String[] args) {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            log.error("Env-variable TELEGRAM_BOT_TOKEN is null or blank!");
            System.exit(1);
        }

        try {
            Parser parser = new ParserImpl();
            ChatMessageAnalyzer analyzer = new ChatMessageAnalyzerImpl();
            ReportRenderer renderer = new ReportRendererImpl();

            ChatProcService processingService = new ChatProcService(parser, analyzer, renderer);
            ChuserBot bot = new ChuserBot(botToken, processingService);

            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, bot);
            log.info("Chuser bot has successfully launched");
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot!", e);
            System.exit(1);
        } catch (Exception e) {
            log.error("ChuserBotApplication: unexpected error!", e);
            System.exit(1);
        }
    }
}
