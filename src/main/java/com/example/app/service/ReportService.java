// src/main/java/com/example/app/service/ReportService.java
package com.example.app.service;

import com.example.app.dto.AnswerReportDto;
import com.example.app.dto.ChildReportDto;
import com.example.app.models.ChildAnswer;
import com.example.app.models.Question;
import com.example.app.models.User;
import com.example.app.repository.ChildAnswerRepository;
import com.example.app.repository.QuestionRepository;
import com.example.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ChildAnswerRepository childAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public ChildReportDto generateDailyReport(UUID childId, LocalDate date) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalStateException("Child not found: " + childId));
        String fullName = child.getFirstName() + " " + child.getLastName();

        List<ChildAnswer> answers = childAnswerRepository
                .findByChildIdAndQuizDateOrderByAnsweredAt(childId, date);

        List<AnswerReportDto> reportEntries = answers.stream()
                .map(ca -> {
                    Question q = questionRepository
                            .findById(ca.getQuestionId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Question not found: " + ca.getQuestionId()
                            ));
                    return new AnswerReportDto(
                            q.getCategory(),
                            q.getPrompt(),
                            ca.getAnswer()
                    );
                })
                .collect(Collectors.toList());

        int score = computeScore(reportEntries);

        return new ChildReportDto(childId, fullName, date, reportEntries, score);
    }

    public List<ChildReportDto> getAllReports(UUID childId) {
        List<LocalDate> dates = childAnswerRepository.findDistinctQuizDatesByChildId(childId);
        return dates.stream()
                .map(dt -> generateDailyReport(childId, dt))
                .collect(Collectors.toList());
    }

    private int computeScore(List<AnswerReportDto> answers) {
        return answers.stream()
                .mapToInt(ans -> {
                    String a = ans.getAnswer().trim().toLowerCase();

                    if (a.equals("sad") || a.equals("😢")) {
                        return 1;
                    } else if (a.equals("neutral") || a.equals("😐")) {
                        return 2;
                    } else if (a.equals("happy") || a.equals("😊")) {
                        return 3;
                    }

                    if (a.equals("da") || a.equals("yes")) {
                        return 3;
                    } else if (a.equals("nu") || a.equals("no")) {
                        return 1;
                    }

                    return 0;
                })
                .sum();
    }
}
