package com.couple.question_answer.repository;

import com.couple.question_answer.entity.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    @Query("{'date': ?0}")
    Optional<Question> findByDate(LocalDate date);

    @Query("{'date': ?0}")
    List<Question> findAllByDate(LocalDate date);

    @Query("{'date': ?0}")
    List<Question> findAllByDateOrderByCreatedAtDesc(LocalDate date);
}