package com.couple.user_couple.service;

import com.couple.user_couple.dto.UserSignupRequest;
import com.couple.user_couple.entity.User;
import com.couple.user_couple.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public User signup(UserSignupRequest request) {
        // 이미 존재하는 사용자인지 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        User.OAuthProvider provider = User.OAuthProvider.valueOf(request.getOauthProvider().toUpperCase());

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .profileImage(request.getProfileImage())
                .birthDate(request.getBirthDate())
                .oauthProvider(provider)
                .oauthId(request.getOauthId())
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public User getUserByOAuth(String provider, String oauthId) {
        User.OAuthProvider oauthProvider = User.OAuthProvider.valueOf(provider.toUpperCase());
        return userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId)
                .orElse(null);
    }

    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}