package com.example.app.models;

import com.example.app.models.enums.QuestionCondition;
import com.example.app.models.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue
    private UUID id;

    private String category;
    private int orderIndex;

    private String prompt;
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private boolean isPrimary;
    private UUID parentQuestionId;
    @Enumerated(EnumType.STRING)
    private QuestionCondition condition;
}

