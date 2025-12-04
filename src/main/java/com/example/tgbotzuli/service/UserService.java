package com.example.tgbotzuli.service;

import com.example.tgbotzuli.model.UserEntity;
import com.example.tgbotzuli.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity getOrCreateUser(User telegramUser) {
        Long chatId = telegramUser.getId();

        return userRepository.findById(chatId)
                .orElseGet(() -> createUser(telegramUser));
    }

    @Transactional
    public UserEntity createUser(User telegramUser) {
        UserEntity user = new UserEntity();
        user.setChatId(telegramUser.getId());
        user.setUsername(telegramUser.getUserName());
        user.setFirstName(telegramUser.getFirstName());
        user.setLastName(telegramUser.getLastName());
        user.setLanguageCode(telegramUser.getLanguageCode());
        user.setIsBot(telegramUser.getIsBot());
        user.setState("START");

        UserEntity savedUser = userRepository.save(user);
        log.info("Created new user: {}", savedUser);
        return savedUser;
    }

    @Transactional
    public void updateUserState(Long chatId, String state) {
        userRepository.updateUserState(chatId, state);
    }

    @Transactional
    public void updateLastActivity(Long chatId) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public Optional<UserEntity> getUser(Long chatId) {
        return userRepository.findById(chatId);
    }

    public long getActiveUsersCount() {
        return userRepository.countByIsActiveTrue();
    }

    @Transactional
    public void cleanupInactiveUsers(int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        userRepository.deactivateInactiveUsers(cutoffDate);
    }
}
