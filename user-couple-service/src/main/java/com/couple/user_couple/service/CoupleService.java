package com.couple.user_couple.service;

import com.couple.common.security.JwtTokenProvider;
import com.couple.user_couple.dto.CoupleDateRequest;
import com.couple.user_couple.dto.CoupleMatchRequest;
import com.couple.user_couple.dto.CoupleMatchAcceptRequest;
import com.couple.user_couple.dto.CoupleResponse;
import com.couple.user_couple.dto.HomeInfoResponse;
import com.couple.user_couple.dto.CoupleMemberResponse;
import com.couple.user_couple.entity.Couple;
import com.couple.user_couple.entity.User;
import com.couple.user_couple.entity.UserToken;
import com.couple.user_couple.repository.CoupleRepository;
import com.couple.user_couple.repository.UserRepository;
import com.couple.user_couple.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoupleService {

        private final CoupleRepository coupleRepository;
        private final UserRepository userRepository;
        private final UserTokenRepository userTokenRepository;
        private final StringRedisTemplate redisTemplate;
        private final JwtTokenProvider jwtTokenProvider;

        private static final String MATCH_CODE_PREFIX = "couple:match:";
        private static final int MATCH_CODE_EXPIRE_SECONDS = 300; // 5분

        public Map<String, String> generateMatchCode(UUID userId, CoupleMatchRequest request) {
                log.info("커플 매칭 코드 생성 요청: {}", userId);

                // 사용자 확인
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

                // 이미 커플이 있는지 확인
                if (user.getCoupleId() != null) {
                        throw new RuntimeException("이미 커플이 있는 사용자입니다.");
                }

                // 커플 미리 생성
                Couple.CoupleBuilder coupleBuilder = Couple.builder()
                                .name(request.getName())
                                .expired("N");
                if (request.getStartDate() != null) {
                        coupleBuilder.startDate(request.getStartDate().atStartOfDay());
                        coupleBuilder.year(request.getStartDate().getYear());
                        coupleBuilder.month(request.getStartDate().getMonthValue());
                        coupleBuilder.date(request.getStartDate().getDayOfMonth());
                } else {
                        coupleBuilder.startDate(java.time.LocalDateTime.now());
                }
                Couple savedCouple = coupleRepository.save(coupleBuilder.build());

                // 사용자 A를 커플에 포함
                user.setCoupleId(savedCouple.getId());
                userRepository.save(user);

                // totalScores 갱신
                var users = userRepository.findByCoupleId(savedCouple.getId());
                long total = users.stream().mapToLong(u -> u.getScore() != null ? u.getScore() : 0L).sum();
                savedCouple.setTotalScores(total);
                coupleRepository.save(savedCouple);

                // 기존 토큰들 만료 처리
                userTokenRepository.deleteAllByUserId(user.getId());

                // 새로운 JWT 토큰 생성 (coupleId 포함)
                String newToken = jwtTokenProvider.generateToken(user.getId(), user.getCoupleId());
                String refreshToken = "refresh_token_" + user.getId() + "_" + System.currentTimeMillis();
                log.info("사용자 A 새로운 토큰 생성: userId={}, coupleId={}", user.getId(), user.getCoupleId());

                // 토큰을 데이터베이스에 저장
                UserToken userToken = UserToken.builder()
                                .userId(user.getId())
                                .accessToken(newToken)
                                .refreshToken(refreshToken)
                                .expiredAt(LocalDateTime.now().plusDays(7)) // 7일 후 만료
                                .build();
                userTokenRepository.save(userToken);
                log.info("사용자 A 토큰 저장 완료: {}", user.getId());

                // 매칭 코드 생성 (6자리 숫자)
                String matchCode = String.format("%06d", (int) (Math.random() * 1000000));

                // Redis에 매칭 정보 저장 (coupleId 포함)
                String redisKey = MATCH_CODE_PREFIX + matchCode;
                String matchData = userId.toString() + ":" + request.getName() + ":"
                                + (request.getStartDate() != null ? request.getStartDate().toString() : "") + ":"
                                + savedCouple.getId().toString();
                redisTemplate.opsForValue().set(redisKey, matchData, MATCH_CODE_EXPIRE_SECONDS,
                                java.util.concurrent.TimeUnit.SECONDS);

                // 응답 데이터 생성
                Map<String, String> response = new HashMap<>();
                response.put("matchCode", matchCode);
                response.put("newToken", newToken);
                response.put("coupleId", savedCouple.getId().toString());

                log.info("매칭 코드 생성 완료: {} - 커플 ID: {} - 새로운 토큰 생성 및 저장", matchCode, savedCouple.getId());
                return response;
        }

        public Map<String, String> acceptMatchCode(UUID userId, CoupleMatchAcceptRequest request) {
                log.info("커플 매칭 수락 요청: {}", userId);

                // 사용자 확인
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

                // 이미 커플이 있는지 확인
                if (user.getCoupleId() != null) {
                        throw new RuntimeException("이미 커플이 있는 사용자입니다.");
                }

                // Redis에서 매칭 정보 조회
                String redisKey = MATCH_CODE_PREFIX + request.getMatchingCode();
                String matchData = redisTemplate.opsForValue().get(redisKey);

                if (matchData == null) {
                        throw new RuntimeException("유효하지 않은 매칭 코드입니다.");
                }

                String[] parts = matchData.split(":");
                UUID requesterId = UUID.fromString(parts[0]);
                String coupleName = parts[1];
                java.time.LocalDate startDate = null;

                if (parts.length > 2 && !parts[2].isEmpty()) {
                        startDate = java.time.LocalDate.parse(parts[2]);
                }

                // 커플 ID 추출
                UUID existingCoupleId = null;
                if (parts.length > 3 && !parts[3].isEmpty()) {
                        existingCoupleId = UUID.fromString(parts[3]);
                }

                // 자기 자신과 매칭 시도 방지
                if (requesterId.equals(userId)) {
                        throw new RuntimeException("자기 자신과는 매칭할 수 없습니다.");
                }

                // 기존 커플에 사용자 B 추가
                final UUID coupleId = existingCoupleId;
                Couple existingCouple = coupleRepository.findById(coupleId)
                                .orElseThrow(() -> new RuntimeException("커플을 찾을 수 없습니다: " + coupleId));

                // 사용자 B를 기존 커플에 추가
                user.setCoupleId(existingCouple.getId());
                userRepository.save(user);

                // totalScores 갱신
                var users = userRepository.findByCoupleId(existingCouple.getId());
                long total = users.stream().mapToLong(u -> u.getScore() != null ? u.getScore() : 0L).sum();
                existingCouple.setTotalScores(total);
                coupleRepository.save(existingCouple);

                // 새로운 JWT 토큰 생성 (coupleId 포함)
                String newToken = jwtTokenProvider.generateToken(user.getId(), user.getCoupleId());
                String refreshToken = "refresh_token_" + user.getId() + "_" + System.currentTimeMillis();
                log.info("사용자 B 새로운 토큰 생성: userId={}, coupleId={}", user.getId(), user.getCoupleId());

                // 기존 토큰들 만료 처리
                userTokenRepository.deleteAllByUserId(user.getId());

                // 토큰을 데이터베이스에 저장
                UserToken userToken = UserToken.builder()
                                .userId(user.getId())
                                .accessToken(newToken)
                                .refreshToken(refreshToken)
                                .expiredAt(LocalDateTime.now().plusDays(7)) // 7일 후 만료
                                .build();
                userTokenRepository.save(userToken);
                log.info("사용자 B 토큰 저장 완료: {}", user.getId());

                // Redis에서 매칭 정보 삭제
                redisTemplate.delete(redisKey);

                // 응답 데이터 생성
                Map<String, String> response = new HashMap<>();
                response.put("newToken", newToken);
                response.put("coupleId", existingCouple.getId().toString());

                log.info("커플 매칭 완료: {} - 사용자 B 추가됨 - 새로운 토큰 생성", existingCouple.getId());
                return response;
        }

        public void breakCouple(UUID userId) {
                log.info("커플 파기 요청: {}", userId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

                Couple couple = coupleRepository.findById(user.getCoupleId())
                                .orElseThrow(() -> new RuntimeException("커플을 찾을 수 없습니다: " + user.getCoupleId()));

                // 커플 상태를 만료로 변경
                couple.setExpired("Y");
                couple.setExpiredAt(LocalDateTime.now());
                coupleRepository.save(couple);

                // 커플의 모든 사용자 coupleId 초기화
                user.setCoupleId(UUID.randomUUID()); // 임시 UUID
                userRepository.save(user);

                // 파트너도 초기화
                User partner = userRepository.findPartnerByCoupleIdAndUserId(couple.getId(), userId)
                                .orElse(null);
                if (partner != null) {
                        partner.setCoupleId(UUID.randomUUID()); // 임시 UUID
                        userRepository.save(partner);
                }

                log.info("커플 파기 완료: {}", couple.getId());
        }

        public void setCoupleDate(UUID userId, CoupleDateRequest request) {
                log.info("커플 날짜 설정 요청: {}", userId);

                // 사용자 확인
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

                // 커플 확인
                if (user.getCoupleId() == null) {
                        throw new RuntimeException("커플이 없는 사용자입니다.");
                }

                Couple couple = coupleRepository.findById(user.getCoupleId())
                                .orElseThrow(() -> new RuntimeException("커플을 찾을 수 없습니다: " + user.getCoupleId()));

                // 날짜 설정
                couple.setCustomDate(request.getDate());
                couple.setYear(request.getDate().getYear());
                couple.setMonth(request.getDate().getMonthValue());
                couple.setDate(request.getDate().getDayOfMonth());
                coupleRepository.save(couple);

                log.info("커플 날짜 설정 완료: {}", request.getDate());
        }

        public HomeInfoResponse getHomeInfo(UUID userId) {
                log.info("홈 정보 조회: {}", userId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

                Couple couple = coupleRepository.findById(user.getCoupleId())
                                .orElseThrow(() -> new RuntimeException("커플을 찾을 수 없습니다: " + user.getCoupleId()));

                // 파트너 정보 조회
                User partner = userRepository.findPartnerByCoupleIdAndUserId(couple.getId(), userId)
                                .orElse(null);

                // 디데이 계산
                long daysSinceStart = ChronoUnit.DAYS.between(couple.getStartDate(), LocalDateTime.now());

                return HomeInfoResponse.builder()
                                .coupleId(couple.getId())
                                .coupleName(couple.getName())
                                .characterId(couple.getCharacterId())
                                .daysSinceStart(daysSinceStart)
                                .partner(partner != null ? convertToUserResponse(partner) : null)
                                .build();
        }

        public CoupleResponse getCoupleInfo(UUID userId) {
                log.info("커플 정보 조회: {}", userId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

                Couple couple = coupleRepository.findById(user.getCoupleId())
                                .orElseThrow(() -> new RuntimeException("커플을 찾을 수 없습니다: " + user.getCoupleId()));

                long daysSinceStart = ChronoUnit.DAYS.between(couple.getStartDate(), LocalDateTime.now());

                return CoupleResponse.builder()
                                .id(couple.getId())
                                .name(couple.getName())
                                .characterId(couple.getCharacterId())
                                .startDate(couple.getStartDate())
                                .customDate(couple.getCustomDate())
                                .year(couple.getYear())
                                .month(couple.getMonth())
                                .date(couple.getDate())
                                .expiredAt(couple.getExpiredAt())
                                .expired(couple.getExpired())
                                .daysSinceStart(daysSinceStart)
                                .build();
        }

        public List<CoupleMemberResponse> getCoupleMembers(UUID userId) {
                log.info("커플 멤버 정보 조회: {}", userId);

                // 사용자 확인
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

                // 커플 확인
                if (user.getCoupleId() == null) {
                        throw new RuntimeException("커플이 없는 사용자입니다.");
                }

                // 커플에 속한 모든 유저 조회
                List<User> users = userRepository.findByCoupleId(user.getCoupleId());

                // DTO로 변환
                return users.stream()
                                .map(this::convertToCoupleMemberResponse)
                                .collect(java.util.stream.Collectors.toList());
        }

        private CoupleMemberResponse convertToCoupleMemberResponse(User user) {
                return CoupleMemberResponse.builder()
                                .userId(user.getId())
                                .gender(user.getGender())
                                .birth(user.getBirth())
                                .build();
        }

        private com.couple.user_couple.dto.UserResponse convertToUserResponse(User user) {
                return com.couple.user_couple.dto.UserResponse.builder()
                                .id(user.getId())
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