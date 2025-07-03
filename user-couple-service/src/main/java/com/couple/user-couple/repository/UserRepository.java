package com.couple.user_couple.repository;

import com.couple.user_couple.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByOauthProviderAndOauthId(User.OAuthProvider provider, String oauthId);

    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByIdWithCouple(@Param("userId") Long userId);

    boolean existsByEmail(String email);
}