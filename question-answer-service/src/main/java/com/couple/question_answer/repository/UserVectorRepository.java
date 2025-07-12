package com.couple.question_answer.repository;

import com.couple.question_answer.entity.UserVector;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVectorRepository extends MongoRepository<UserVector, String> {

    Optional<UserVector> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
    
    // MongoDB에서 UUID가 인코딩되는 문제를 해결하기 위한 문자열 기반 조회
    @Query("{'user_id': ?0}")
    Optional<UserVector> findByUserIdString(String userId);
    
    @Query("{'user_id': ?0}")
    boolean existsByUserIdString(String userId);
}