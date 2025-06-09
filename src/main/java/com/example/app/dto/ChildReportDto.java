package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ChildReportDto {
    private UUID childId;
    private String childName;
    private LocalDate date;
    private List<AnswerReportDto> answers;
    private int score;

}
