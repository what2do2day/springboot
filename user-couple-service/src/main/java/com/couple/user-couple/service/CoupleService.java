package com.couple.user_couple.service;

import com.couple.user_couple.dto.CoupleMatchRequest;
import com.couple.user_couple.entity.Couple;
import com.couple.user_couple.entity.User;
import com.couple.user_couple.repository.CoupleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    private static final String MATCH_CODE_PREFIX = "couple:match:";
    private static final int MATCH_CODE_EXPIRE_SECONDS = 300; // 5분

    public String generateMatchCode(Long userId) {
        // 이미 커플이 있는지 확인
        if (coupleRepository.findByUserIdAndActive(userId).isPresent()) {
            throw new RuntimeException("이미 커플이 있습니다.");
        }

        String matchCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String redisKey = MATCH_CODE_PREFIX + matchCode;

        // Redis에 매칭 코드 저장 (5분 만료)
        redisTemplate.opsForValue().set(redisKey, userId.toString());
        redisTemplate.expire(redisKey, java.time.Duration.ofSeconds(MATCH_CODE_EXPIRE_SECONDS));

        return matchCode;
    }

    public Couple matchCouple(Long userId, CoupleMatchRequest request) {
        String redisKey = MATCH_CODE_PREFIX + request.getMatchCode();
        String creatorUserId = redisTemplate.opsForValue().get(redisKey);

        if (creatorUserId == null) {
            throw new RuntimeException("유효하지 않거나 만료된 매칭 코드입니다.");
        }

        Long creatorId = Long.parseLong(creatorUserId);

        // 자기 자신과 매칭하려는 경우
        if (creatorId.equals(userId)) {
            throw new RuntimeException("자기 자신과는 매칭할 수 없습니다.");
        }

        // 이미 커플이 있는지 확인
        if (coupleRepository.findByUserIdAndActive(userId).isPresent()) {
            throw new RuntimeException("이미 커플이 있습니다.");
        }

        User user1 = userService.getUserById(creatorId);
        User user2 = userService.getUserById(userId);

        Couple couple = Couple.builder()
                .user1(user1)
                .user2(user2)
                .startDate(LocalDateTime.now())
                .status(Couple.CoupleStatus.ACTIVE)
                .build();

        // Redis에서 매칭 코드 삭제
        redisTemplate.delete(redisKey);

        return coupleRepository.save(couple);
    }

    @Transactional(readOnly = true)
    public Couple getCoupleByUserId(Long userId) {
        return coupleRepository.findByUserIdAndActive(userId)
                .orElse(null);
    }

    public void breakUpCouple(Long userId) {
        Couple couple = coupleRepository.findByUserIdAndActive(userId)
                .orElseThrow(() -> new RuntimeException("커플 정보를 찾을 수 없습니다."));

        couple.setStatus(Couple.CoupleStatus.BREAKUP);
        coupleRepository.save(couple);
    }
}