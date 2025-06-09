package com.example.app.repository;

import com.example.app.models.ChildAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ChildAnswerRepository extends JpaRepository<ChildAnswer, Long> {
    long countByChildIdAndQuizDate(UUID childId, LocalDate quizDate);

    List<ChildAnswer> findByChildIdAndQuizDateOrderByAnsweredAt(UUID childId, LocalDate quizDate);

    @Query("SELECT DISTINCT c.quizDate FROM ChildAnswer c WHERE c.childId = :childId ORDER BY c.quizDate DESC")
    List<LocalDate> findDistinctQuizDatesByChildId(@Param("childId") UUID childId);
}
