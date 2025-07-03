package com.couple.user_couple.repository;

import com.couple.user_couple.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    @Query("SELECT ut FROM UserToken ut WHERE ut.userId = :userId AND ut.expiredAt > :now ORDER BY ut.issuedAt DESC")
    List<UserToken> findValidTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("SELECT ut FROM UserToken ut WHERE ut.accessToken = :accessToken AND ut.expiredAt > :now")
    Optional<UserToken> findValidTokenByAccessToken(@Param("accessToken") String accessToken,
            @Param("now") LocalDateTime now);

    @Query("SELECT ut FROM UserToken ut WHERE ut.refreshToken = :refreshToken AND ut.expiredAt > :now")
    Optional<UserToken> findValidTokenByRefreshToken(@Param("refreshToken") String refreshToken,
            @Param("now") LocalDateTime now);

    void deleteAllByUserId(UUID userId);

    @Query("DELETE FROM UserToken ut WHERE ut.expiredAt <= :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}