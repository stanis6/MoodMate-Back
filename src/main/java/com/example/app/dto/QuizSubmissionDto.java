package com.example.app.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Data
public class QuizSubmissionDto {
    private UUID childId;
    private LocalDate quizDate;
    private List<AnswerDto> answers;
}
