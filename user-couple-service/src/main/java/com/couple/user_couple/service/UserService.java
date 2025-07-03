package com.couple.user_couple.service;

import com.couple.common.security.JwtTokenProvider;
import com.couple.user_couple.dto.UserSignupRequest;
import com.couple.user_couple.dto.UserUpdateRequest;
import com.couple.user_couple.dto.UserResponse;
import com.couple.user_couple.dto.LoginRequest;
import com.couple.user_couple.entity.User;
import com.couple.user_couple.entity.UserToken;
import com.couple.user_couple.repository.UserRepository;
import com.couple.user_couple.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserResponse signup(UserSignupRequest request) {
        log.info("회원가입 요청: {}", request.getEmail());

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다: " + request.getEmail());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .gender(request.getGender())
                .birth(request.getBirth().toString())
                .fcmCode(request.getFcmCode())
                .sendTime(request.getSendTime().toString())
                .coupleId(null) // 커플 매칭 시 실제 값으로 변경
                .build();

        // birth에서 년, 월, 일 추출하여 저장
        user.setYear(request.getBirth().getYear());
        user.setMonth(request.getBirth().getMonthValue());
        user.setDate(request.getBirth().getDayOfMonth());

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    public UserResponse login(LoginRequest request) {
        log.info("로그인 요청: {}", request.getEmail());

        try {
            // 이메일로 사용자 찾기
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + request.getEmail()));

            log.info("사용자 찾음: {}", user.getName());

            // 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("비밀번호 불일치: {}", request.getEmail());
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }

            log.info("비밀번호 검증 성공: {}", request.getEmail());

            // 기존 토큰들 만료 처리
            userTokenRepository.deleteAllByUserId(user.getId());

            // 실제 JWT 토큰 생성
            String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getCoupleId());
            String refreshToken = "refresh_token_" + user.getId() + "_" + System.currentTimeMillis();

            UserToken userToken = UserToken.builder()
                    .userId(user.getId())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiredAt(LocalDateTime.now().plusDays(7)) // 7일 후 만료
                    .build();

            userTokenRepository.save(userToken);
            log.info("토큰 저장 완료: {}", user.getId());

            UserResponse response = convertToResponse(user);
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);

            log.info("로그인 성공: {}", request.getEmail());
            return response;

        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserResponse getUserInfo(UUID userId) {
        log.info("사용자 정보 조회: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        return convertToResponse(user);
    }

    public UserResponse updateUserInfo(UUID userId, UserUpdateRequest request) {
        log.info("사용자 정보 수정: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        user.setName(request.getName());
        user.setFcmCode(request.getFcmCode());
        user.setSendTime(request.getSendTime().toString());

        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    public void deleteUser(UUID userId) {
        log.info("사용자 탈퇴: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        userRepository.delete(user);
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .coupleId(user.getCoupleId())
                .score(user.getScore())
                .gender(user.getGender())
                .sendTime(user.getSendTime())
                .fcmCode(user.getFcmCode())
                .birth(user.getBirth())
                .year(user.getYear())
                .month(user.getMonth())
                .date(user.getDate())
                .build();
    }
}