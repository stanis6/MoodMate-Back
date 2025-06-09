package com.example.app.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Getter
@Setter
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = true)
    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private boolean confirmed;

    @Column(nullable = false)
    private String parentEmail;

    @Column(nullable = false)
    private String childFirstName;

    @Column(nullable = false)
    private String childLastName;
}