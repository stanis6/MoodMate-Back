package com.example.app.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AnswerDto {
    private UUID questionId;
    private String answer;
}
