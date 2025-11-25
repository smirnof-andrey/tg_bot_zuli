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
            log.info("New message received: " + update.getMessage().getText());
            log.info("Message: " + update);
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();

            // Обработка ответов на предыдущие сообщения
            if (userStates.containsKey(chatId)) {
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
            case "/login":
                log.info("Logged in");
                startSurvey(chatId);
                break;
            default:
                sendMessage(chatId, "Неизвестная команда");
        }
    }

    private void sendStartMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Приветик! Это бот Zuli❤" +
                "Смотри расписание тренировок, записывайся и приходи тренить\uD83D\uDE09");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
        message.setText("Спасибо, " + name + "! Сколько вам лет?");

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
        message.setText("Спасибо за ответы! Ваш возраст: " + age);

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
