package com.couple.question_answer.repository;

import com.couple.question_answer.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {

    List<UserAnswer> findByUserId(UUID userId);

    List<UserAnswer> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<UserAnswer> findByCoupleId(UUID coupleId);

    List<UserAnswer> findByUserIdAndCoupleId(UUID userId, UUID coupleId);
}