package com.couple.question_answer.repository;

import com.couple.question_answer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 현재 시각과 일치하는 send_time을 가진 유저들을 조회
     * @param currentTime "HH:mm" 형식의 현재 시각
     * @return 해당 시각에 알림을 받을 유저 목록
     */
    @Query("SELECT u FROM User u WHERE u.sendTime = :currentTime")
    List<User> findBySendTime(@Param("currentTime") String currentTime);
    
    /**
     * FCM 토큰이 유효한 유저들을 조회
     * @param currentTime "HH:mm" 형식의 현재 시각
     * @return FCM 토큰이 유효한 유저 목록
     */
    @Query("SELECT u FROM User u WHERE u.sendTime = :currentTime AND u.fcmToken IS NOT NULL AND u.fcmToken != ''")
    List<User> findUsersWithValidFcmTokenBySendTime(@Param("currentTime") String currentTime);
} 