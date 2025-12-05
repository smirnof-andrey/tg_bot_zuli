package com.example.tgbotzuli.config;

import com.example.tgbotzuli.service.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class BotConfig {
    private final String botToken;

    public BotConfig(@Value("${telegram.bot.token}") String botToken) {
        log.info("BotToken: " + botToken);
        if (botToken == null || botToken.equals("default_fallback_token")) {
            throw new IllegalStateException("BOT_TOKEN not configured properly");
        }
        this.botToken = botToken;
    }

    public String getBotToken() {
        return botToken;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService bot) throws TelegramApiException {
        log.info("TelegramBot: " + bot.getBotUsername() + ", " + bot.getBotToken());
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }
}
