package com.example.app.controller;

import com.example.app.dto.QuestionDto;
import com.example.app.dto.QuizSubmissionDto;
import com.example.app.security.JwtUtil;
import com.example.app.service.QuestionService;
import com.example.app.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuestionService questionService;
    private final QuizService quizService;

    public QuizController(QuestionService questionService, QuizService quizService) {
        this.questionService = questionService;
        this.quizService = quizService;
    }

    @GetMapping("/today")
    public List<QuestionDto> getTodaysQuiz() {
        return questionService.getAllQuestionsOrdered()
                .stream()
                .map(QuestionDto::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@RequestBody QuizSubmissionDto submission) {
        quizService.saveAnswers(submission);
        return ResponseEntity.ok().body("{\"message\":\"Quiz saved\"}");
    }

    @GetMapping("/completed")
    public ResponseEntity<Map<String, Boolean>> checkCompleted(
            @RequestHeader("Authorization") String authHeader) {

        String jwtToken = authHeader.substring(7);
        UUID childId = JwtUtil.extractUserId(jwtToken);

        boolean done = quizService.hasSubmittedToday(childId);
        return ResponseEntity.ok(Map.of("completed", done));
    }
}
