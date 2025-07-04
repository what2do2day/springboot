package com.couple.question_answer.repository;

import com.couple.question_answer.entity.UserTagProfile;
import com.couple.question_answer.entity.UserTagProfileId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserTagProfileRepository extends JpaRepository<UserTagProfile, UserTagProfileId> {

    @Query("SELECT utp FROM UserTagProfile utp WHERE utp.id.userId = :userId")
    List<UserTagProfile> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT utp FROM UserTagProfile utp WHERE utp.id.userId = :userId AND utp.id.tagId = :tagId")
    UserTagProfile findByUserIdAndTagId(@Param("userId") UUID userId, @Param("tagId") UUID tagId);
}