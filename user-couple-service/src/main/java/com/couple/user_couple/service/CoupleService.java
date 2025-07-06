package com.couple.user_couple.service;

import com.couple.user_couple.dto.CoupleDateRequest;
import com.couple.user_couple.dto.CoupleMatchRequest;
import com.couple.user_couple.dto.CoupleMatchAcceptRequest;
import com.couple.user_couple.dto.CoupleResponse;
import com.couple.user_couple.dto.HomeInfoResponse;
import com.couple.user_couple.dto.CoupleMemberResponse;
import com.couple.user_couple.dto.UserResponse;
import com.couple.user_couple.entity.Couple;
import com.couple.user_couple.entity.User;
import com.couple.user_couple.repository.CoupleRepository;
import com.couple.user_couple.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoupleService {

        private final CoupleRepository coupleRepository;
        private final UserRepository userRepository;
        private final StringRedisTemplate redisTemplate;
        private final RestTemplate restTemplate;

        private static final String MATCH_CODE_PREFIX = "couple:match:";
        private static final int MATCH_CODE_EXPIRE_SECONDS = 300; // 5분

        public String generateMatchCode(UUID userId, CoupleMatchRequest request) {
                log.info("커플 매칭 코드 생성 요청: {}", userId);

                // 사용자 확인
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

                // 이미 커플이 있는지 확인
                if (user.getCoupleId() != null) {
                        throw new RuntimeException("이미 커플이 있는 사용자입니다.");
                }

                // 매칭 코드 생성 (6자리 숫자)
                String matchCode = String.format("%06d", (int) (Math.random() * 1000000));

                // Redis에 매칭 정보 저장 (characterId 제거)
                String redisKey = MATCH_CODE_PREFIX + matchCode;
                String matchData = userId.toString() + ":" + request.getName() + ":"
                                + (request.getStartDate() != null ? request.getStartDate().toString() : "");
                redisTemplate.opsForValue().set(redisKey, matchData, MATCH_CODE_EXPIRE_SECONDS,
                                java.util.concurrent.TimeUnit.SECONDS);

                log.info("매칭 코드 생성 완료: {}", matchCode);
                return matchCode;
        }

        public void acceptMatchCode(UUID userId, CoupleMatchAcceptRequest request) {
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

                // 자기 자신과 매칭 시도 방지
                if (requesterId.equals(userId)) {
                        throw new RuntimeException("자기 자신과는 매칭할 수 없습니다.");
                }

                // 커플 생성 (characterId는 엔티티 기본값 사용)
                Couple.CoupleBuilder coupleBuilder = Couple.builder()
                                .name(coupleName)
                                .expired("N");
                if (startDate != null) {
                        coupleBuilder.startDate(startDate.atStartOfDay());
                        coupleBuilder.year(startDate.getYear());
                        coupleBuilder.month(startDate.getMonthValue());
                        coupleBuilder.date(startDate.getDayOfMonth());
                } else {
                        coupleBuilder.startDate(java.time.LocalDateTime.now());
                }
                Couple savedCouple = coupleRepository.save(coupleBuilder.build());

                // 두 사용자의 coupleId 업데이트
                User requester = userRepository.findById(requesterId)
                                .orElseThrow(() -> new RuntimeException("매칭 요청자를 찾을 수 없습니다."));

                requester.setCoupleId(savedCouple.getId());
                user.setCoupleId(savedCouple.getId());

                userRepository.save(requester);
                userRepository.save(user);

                // totalScores 갱신
                var users = userRepository.findByCoupleId(savedCouple.getId());
                long total = users.stream().mapToLong(u -> u.getScore() != null ? u.getScore() : 0L).sum();
                savedCouple.setTotalScores(total);
                coupleRepository.save(savedCouple);

                // Redis에서 매칭 정보 삭제
                redisTemplate.delete(redisKey);

                // couple-chat-service에 채팅방 생성 요청
                try {
                    String chatServiceUrl = "http://couple-chat-service:8084/api/v1/couple-chat/rooms";
                    String requestUrl = String.format("%s?coupleId=%s&user1Id=%s&user2Id=%s", 
                        chatServiceUrl, savedCouple.getId(), requesterId, userId);
                    
                    restTemplate.postForEntity(requestUrl, null, Object.class);
                    log.info("채팅방 생성 완료: coupleId={}", savedCouple.getId());
                } catch (Exception e) {
                    log.error("채팅방 생성 실패: {}", e.getMessage());
                    // 채팅방 생성 실패는 커플 생성에 영향을 주지 않도록 함
                }

                log.info("커플 매칭 완료: {}", savedCouple.getId());
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
                                .coupleId(user.getCoupleId())
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