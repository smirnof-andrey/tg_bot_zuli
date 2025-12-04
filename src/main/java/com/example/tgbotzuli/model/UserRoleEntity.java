package com.example.tgbotzuli.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_roles")
@Data
public class UserRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_chat_id", referencedColumnName = "chat_id")
    private UserEntity userEntity;

    @Column(name = "role")
    private String role; // "USER", "ADMIN", "MODERATOR"
}
