package com.couple.question_answer.repository;

import com.couple.question_answer.entity.QuestionTag;
import com.couple.question_answer.entity.QuestionTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionTagRepository extends JpaRepository<QuestionTag, QuestionTagId> {

    @Query("SELECT qt FROM QuestionTag qt WHERE qt.id.questionId = :questionId")
    List<QuestionTag> findByQuestionId(@Param("questionId") UUID questionId);
}