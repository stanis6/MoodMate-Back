package com.example.app.service;

import com.example.app.dto.QuizSubmissionDto;
import com.example.app.models.ChildAnswer;
import com.example.app.repository.ChildAnswerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class QuizService {
    private final ChildAnswerRepository childAnswerRepository;

    public QuizService(ChildAnswerRepository childAnswerRepository) {
        this.childAnswerRepository = childAnswerRepository;
    }

    public void saveAnswers(QuizSubmissionDto submission) {
        submission.getAnswers().forEach(answerDto -> {
            ChildAnswer ca = new ChildAnswer();
            ca.setChildId(submission.getChildId());
            ca.setQuizDate(submission.getQuizDate());
            ca.setQuestionId(answerDto.getQuestionId());
            ca.setAnswer(answerDto.getAnswer());
            ca.setAnsweredAt(LocalDateTime.now());
            childAnswerRepository.save(ca);
        });
    }

    public boolean hasSubmittedToday(UUID childId) {
        long count = childAnswerRepository.countByChildIdAndQuizDate(
                childId,
                LocalDate.now()
        );
        return count > 0;
    }
}
