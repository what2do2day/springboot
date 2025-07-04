package com.couple.question_answer.repository;

import com.couple.question_answer.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {

    @Query("SELECT ua FROM UserAnswer ua WHERE ua.userId = :userId ORDER BY ua.createdAt DESC")
    List<UserAnswer> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT ua FROM UserAnswer ua WHERE ua.userId = :userId AND ua.questionId = :questionId")
    List<UserAnswer> findByUserIdAndQuestionId(@Param("userId") UUID userId, @Param("questionId") UUID questionId);
}