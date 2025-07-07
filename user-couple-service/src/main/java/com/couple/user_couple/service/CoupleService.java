package com.couple.user_couple.service;

import com.couple.user_couple.dto.CoupleDateRequest;
import com.couple.user_couple.dto.CoupleMatchRequest;
import com.couple.user_couple.dto.CoupleMatchAcceptRequest;
import com.couple.user_couple.dto.CoupleResponse;
import com.couple.user_couple.dto.HomeInfoResponse;
import com.couple.user_couple.dto.CoupleMemberResponse;
import com.couple.user_couple.dto.UserResponse;
import com.couple.user_couple.dto.CoupleInfoResponse;
import com.couple.user_couple.dto.CoupleRankResponse;
import com.couple.user_couple.entity.Couple;
import com.couple.user_couple.entity.User;
import com.couple.user_couple.repository.CoupleRepository;
import com.couple.user_couple.repository.UserRepository;
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
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoupleService {

        private final CoupleRepository coupleRepository;
        private final UserRepository userRepository;
        private final StringRedisTemplate redisTemplate;

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

                // 매칭 코드 생성 (6자리 숫자)
                String matchCode = String.format("%06d", (int) (Math.random() * 1000000));

                // Redis에 매칭 정보 저장 (characterId 제거)
                String redisKey = MATCH_CODE_PREFIX + matchCode;
                String matchData = userId.toString() + ":" + request.getName() + ":"
                                + (request.getStartDate() != null ? request.getStartDate().toString() : "");
                redisTemplate.opsForValue().set(redisKey, matchData, MATCH_CODE_EXPIRE_SECONDS,
                                java.util.concurrent.TimeUnit.SECONDS);

                // 응답 데이터 생성
                Map<String, String> response = new HashMap<>();
                response.put("matchCode", matchCode);

                log.info("매칭 코드 생성 완료: {}", matchCode);
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

                // 응답 데이터 생성
                Map<String, String> response = new HashMap<>();
                response.put("coupleId", savedCouple.getId().toString());

                log.info("커플 매칭 완료: {}", savedCouple.getId());
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

        /**
         * 사용자 ID로 커플 정보와 디데이를 조회합니다.
         * 추천을 요청한 사람이 항상 user1이 되도록 합니다.
         * 
         * @param userId 사용자 ID (추천을 요청한 사람)
         * @return 커플 정보와 디데이
         */
        public CoupleInfoResponse getCoupleInfoByUserId(UUID userId) {
                log.info("커플 정보 조회 시작: userId={}", userId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
                log.info("사용자 조회 성공: userId={}, coupleId={}", userId, user.getCoupleId());

                if (user.getCoupleId() == null) {
                        log.error("사용자가 커플에 속하지 않음: userId={}", userId);
                        throw new RuntimeException("커플이 없는 사용자입니다: " + userId);
                }

                Couple couple = coupleRepository.findById(user.getCoupleId())
                                .orElseThrow(() -> new RuntimeException("커플을 찾을 수 없습니다: " + user.getCoupleId()));
                log.info("커플 조회 성공: coupleId={}, coupleName={}, startDate={}", 
                        couple.getId(), couple.getName(), couple.getStartDate());

                // 디데이 계산
                long dday = calculateDday(couple.getId());
                log.info("디데이 계산 완료: dday={}", dday);

                // 커플 멤버 정보 조회
                List<User> users = userRepository.findByCoupleId(couple.getId());
                log.info("커플 멤버 조회: {}명", users.size());
                
                if (users.size() < 2) {
                        log.error("커플 멤버가 부족함: coupleId={}, memberCount={}", couple.getId(), users.size());
                        throw new RuntimeException("커플 멤버가 부족합니다: " + couple.getId());
                }

                // 추천을 요청한 사람을 user1으로, 파트너를 user2로 할당
                User user1 = user; // 추천을 요청한 사람
                User user2 = users.stream()
                                .filter(u -> !u.getId().equals(userId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("파트너를 찾을 수 없습니다: " + userId));
                
                log.info("커플 멤버 정보: user1(요청자)={}, user2(파트너)={}", user1.getId(), user2.getId());

                CoupleInfoResponse response = CoupleInfoResponse.builder()
                                .coupleId(couple.getId())
                                .coupleName(couple.getName())
                                .dday(dday)
                                .startDate(couple.getStartDate() != null ? couple.getStartDate().toString() : null)
                                .user1(CoupleInfoResponse.UserInfo.builder()
                                                .userId(user1.getId())
                                                .name(user1.getName())
                                                .gender(user1.getGender())
                                                .birth(user1.getBirth())
                                                .build())
                                .user2(CoupleInfoResponse.UserInfo.builder()
                                                .userId(user2.getId())
                                                .name(user2.getName())
                                                .gender(user2.getGender())
                                                .birth(user2.getBirth())
                                                .build())
                                .build();
                
                log.info("커플 정보 응답 생성 완료: coupleId={}, dday={}, user1Birth={}, user2Birth={}", 
                        response.getCoupleId(), response.getDday(), 
                        response.getUser1().getBirth(), response.getUser2().getBirth());
                
                return response;
        }

        /**
         * 커플의 디데이를 계산합니다 (사용자 ID로).
         * 
         * @param userId 사용자 ID
         * @return 디데이 (일수)
         */
        public long calculateDdayByUserId(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
                
                if (user.getCoupleId() == null) {
                        throw new RuntimeException("커플이 없는 사용자입니다: " + userId);
                }
                
                return calculateDday(user.getCoupleId());
        }

        /**
         * 커플의 디데이를 계산합니다.
         * 
         * @param coupleId 커플 ID
         * @return 디데이 (일수)
         */
        public long calculateDday(UUID coupleId) {
                Couple couple = coupleRepository.findById(coupleId)
                                .orElseThrow(() -> new RuntimeException("커플을 찾을 수 없습니다: " + coupleId));
                
                // startDate가 null인 경우 처리
                if (couple.getStartDate() == null) {
                        log.warn("커플의 startDate가 null입니다: {}", coupleId);
                        return 0L;
                }
                
                // 한국 시간 기준으로 현재 날짜 계산
                ZoneId koreaZone = ZoneId.of("Asia/Seoul");
                LocalDate startDate = couple.getStartDate().toLocalDate();
                LocalDate currentDate = LocalDate.now(koreaZone);
                
                long daysSinceStart = ChronoUnit.DAYS.between(startDate, currentDate);
                log.info("디데이 계산 완료: coupleId={}, startDate={}, currentDate={}, dday={}", 
                                coupleId, startDate, currentDate, daysSinceStart);
                
                return daysSinceStart;
        }

        /**
         * 전체 커플의 점수 기준 랭킹을 조회합니다.
         * 
         * @return 커플 랭킹 리스트
         */
        public List<CoupleRankResponse> getCoupleRanks() {
                log.info("커플 랭킹 조회 시작");

                // 만료되지 않은 모든 커플 조회
                List<Couple> couples = coupleRepository.findByExpired("N");
                log.info("활성 커플 수: {}", couples.size());

                // 커플별 랭킹 정보 생성 (매칭 완료된 커플만)
                List<CoupleRankResponse> rankList = couples.stream()
                                .map(couple -> {
                                        // 커플의 사용자 조회
                                        List<User> users = userRepository.findByCoupleId(couple.getId());
                                        
                                        // 사용자가 2명이 아닌 경우 null 반환 (나중에 필터링)
                                        if (users.size() != 2) {
                                                return null;
                                        }
                                        
                                        // 커플의 총 점수 계산
                                        long totalScore = users.stream()
                                                        .mapToLong(user -> user.getScore() != null ? user.getScore() : 0L)
                                                        .sum();

                                        // 점수가 0 이하인 경우 null 반환 (나중에 필터링)
                                        if (totalScore <= 0) {
                                                return null;
                                        }

                                        return CoupleRankResponse.builder()
                                                        .coupleName(couple.getName())
                                                        .totalScore(totalScore)
                                                        .rank(0) // 임시로 0 설정, 나중에 정렬 후 순위 부여
                                                        .build();
                                })
                                .filter(rank -> rank != null) // null 값 제거
                                .sorted((r1, r2) -> Long.compare(r2.getTotalScore(), r1.getTotalScore())) // 점수 내림차순 정렬
                                .collect(java.util.stream.Collectors.toList());

                // 순위 부여
                for (int i = 0; i < rankList.size(); i++) {
                        rankList.get(i).setRank(i + 1);
                }

                log.info("커플 랭킹 조회 완료: {}개 커플", rankList.size());
                return rankList;
        }
}