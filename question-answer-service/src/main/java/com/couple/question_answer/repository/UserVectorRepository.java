package com.couple.question_answer.repository;

import com.couple.question_answer.entity.UserVector;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVectorRepository extends MongoRepository<UserVector, String> {

    Optional<UserVector> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}