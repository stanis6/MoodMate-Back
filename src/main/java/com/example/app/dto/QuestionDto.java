package com.example.app.dto;

import com.example.app.models.Question;
import com.example.app.models.enums.QuestionCondition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
public class QuestionDto {
    public UUID id;
    public String category;
    public int orderIndex;
    public String prompt;
    public String type;
    public boolean isPrimary;
    public UUID parentQuestionId;
    public String condition;

    public QuestionDto(Question q) {
        this.id = q.getId();
        this.category = q.getCategory();
        this.orderIndex = q.getOrderIndex();
        this.prompt = q.getPrompt();
        this.type = q.getType().name();
        this.isPrimary = q.isPrimary();
        this.parentQuestionId = q.getParentQuestionId();
        this.condition = q.getCondition() != null ? q.getCondition().name() : null;
    }
}

