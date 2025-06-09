package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnswerReportDto {
    private String category;
    private String questionPrompt;
    private String answer;
}
