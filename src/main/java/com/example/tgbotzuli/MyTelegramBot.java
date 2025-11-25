package com.example.tgbotzuli;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final Map<Long, String> userStates = new HashMap<>();

    @Value("${BOT_TOKEN}")
    private String botToken;

    @Value("${bot.username}")
    private String botUsername;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("Message: " + update);
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();

            // Обработка ответов на предыдущие сообщения
            if (userStates.containsKey(chatId)) {
                log.info("User: " + userStates.get(chatId));
                String previousState = userStates.get(chatId);
                handleUserResponse(chatId, text, previousState);
                userStates.remove(chatId);
            } else {
                handleCommand(chatId, text);
            }
        }
    }

    private void handleCommand(Long chatId, String text) {
        switch (text) {
            case "/start":
                log.info("Starting bot");
                sendStartMessage(chatId);
                break;
            case "/calendar":
                log.info("Calendar");
                sandCalendarMsg(chatId);
                break;
            case "/reserve":
                log.info("Reserve");
                sendMessage(chatId, "Запись на тренировку скоро будет доступна\uD83D\uDE0A");
                break;
            case "/info":
                sendMessage(chatId, "Расскажу про мои тренировки...");
                break;
            case "/login":
                log.info("Logged in");
                startSurvey(chatId);
                break;
            default:
                sendMessage(chatId, "\uD83E\uDD14 ???");
        }
    }

    private void sendStartMessage(Long chatId) {
        sendMessage(chatId, "Приветик! Это бот Zuli❤\n" +
                "Смотри расписание тренировок, записывайся и приходи тренить\uD83D\uDE09");
    }

    private void sandCalendarMsg(Long chatId) {
        sendMessage(chatId, "Следующая тренировка будет проходить <дата> в <место>. Записывайся\uD83D\uDC83");
    }

    private void startSurvey(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Как вас зовут?");

        try {
            execute(message);
            userStates.put(chatId, "AWAITING_NAME");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleUserResponse(Long chatId, String response, String state) {
        switch (state) {
            case "AWAITING_NAME":
                askForAge(chatId, response);
                break;
            case "AWAITING_AGE":
                finishSurvey(chatId, response);
                break;
        }
    }

    private void askForAge(Long chatId, String name) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Спасибо, " + name + "! Как давно тренируешься?");

        try {
            execute(message);
            userStates.put(chatId, "AWAITING_AGE");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void finishSurvey(Long chatId, String age) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Спасибо за ответы! Твой опыт в тренировках: " + age);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
