package com.example.tgbotzuli.service;


import com.example.tgbotzuli.model.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    private final UserService userService;
    private final String botToken;
    private final String botUsername;

    public TelegramBotService(UserService userService,
                              @Value("${telegram.bot.token}") String botToken,
                              @Value("${telegram.bot.username}") String botUsername) {
        super(botToken);
        this.userService = userService;
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();

            // Получаем или создаем пользователя
            UserEntity user = userService.getOrCreateUser(message.getFrom());

            // Обновляем активность
            userService.updateLastActivity(chatId);

            String text = message.getText();
            log.info("Received message from {}: {}", user.getUsername(), text);

            // Обработка команд
            if (text.startsWith("/")) {
                handleCommand(chatId, text, user);
            } else {
                handleMessage(chatId, text, user);
            }
        }
    }

    private void handleCommand(Long chatId, String command, UserEntity user) {
        switch (command) {
            case "/start":
                sendMessage(chatId, "Добро пожаловать, " + user.getFirstName() + "! 🎉");
                userService.updateUserState(chatId, "MAIN_MENU");
                break;

            case "/stats":
                long activeUsers = userService.getActiveUsersCount();
                sendMessage(chatId, "📊 Статистика бота:\nАктивных пользователей: " + activeUsers);
                break;

            case "/profile":
                String profile = String.format(
                        "👤 Ваш профиль:\nID: %d\nИмя: %s\nЮзернейм: %s\nСтатус: %s",
                        user.getChatId(),
                        user.getFirstName(),
                        user.getUsername() != null ? "@" + user.getUsername() : "не указан",
                        user.getState()
                );
                sendMessage(chatId, profile);
                break;

            default:
                sendMessage(chatId, "Неизвестная команда. Используйте /help");
        }
    }

    private void handleMessage(Long chatId, String text, UserEntity user) {
        String response = "Вы сказали: " + text + "\nВаш текущий статус: " + user.getState();
        sendMessage(chatId, response);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to chat {}: {}", chatId, e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
