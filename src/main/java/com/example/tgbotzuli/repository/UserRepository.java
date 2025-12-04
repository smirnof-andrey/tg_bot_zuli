package com.example.tgbotzuli.repository;

import com.example.tgbotzuli.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findByIsActiveTrue();

    List<UserEntity> findByCreatedAtAfter(LocalDateTime date);

    @Modifying
    @Query("UPDATE UserEntity u SET u.state = :state WHERE u.chatId = :chatId")
    void updateUserState(@Param("chatId") Long chatId, @Param("state") String state);

    @Modifying
    @Query("UPDATE UserEntity u SET u.isActive = false WHERE u.lastActivity < :date")
    void deactivateInactiveUsers(@Param("date") LocalDateTime date);

    long countByIsActiveTrue();
}