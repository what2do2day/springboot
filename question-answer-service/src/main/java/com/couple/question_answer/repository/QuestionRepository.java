package com.couple.question_answer.repository;

import com.couple.question_answer.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query("SELECT q FROM Question q WHERE q.date = :date AND q.sentYn = 'N' ORDER BY q.createdAt")
    List<Question> findUnsentQuestionsByDate(@Param("date") LocalDate date);

    @Query("SELECT q FROM Question q WHERE q.sentYn = 'Y' ORDER BY q.sentTime DESC")
    List<Question> findSentQuestions();

    @Query("SELECT q FROM Question q WHERE q.date = :date ORDER BY q.createdAt")
    List<Question> findQuestionsByDate(@Param("date") LocalDate date);
}