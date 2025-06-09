package com.example.app.service;

import com.example.app.models.Question;
import com.example.app.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> getAllQuestionsOrdered() {
        return questionRepository.findAllByOrderByOrderIndexAsc();
    }
}
