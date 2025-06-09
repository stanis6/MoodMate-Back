package com.example.app.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SetPasswordRequest {
    private UUID childId;
    private String newPassword;
}

