package com.couple.question_answer.service;

import com.couple.question_answer.dto.UserTagProfileResponse;
import com.couple.question_answer.entity.Tag;
import com.couple.question_answer.entity.UserTagProfile;
import com.couple.question_answer.repository.TagRepository;
import com.couple.question_answer.repository.UserTagProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserTagProfileService {

    private final UserTagProfileRepository userTagProfileRepository;
    private final TagRepository tagRepository;

    public List<UserTagProfileResponse> getUserTagProfiles(UUID userId) {
        log.info("사용자 태그 프로필 조회: {}", userId);

        List<UserTagProfile> profiles = userTagProfileRepository.findByUserId(userId);
        return profiles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public UserTagProfileResponse getUserTagProfile(UUID userId, UUID tagId) {
        log.info("사용자 특정 태그 프로필 조회: userId={}, tagId={}", userId, tagId);

        UserTagProfile profile = userTagProfileRepository.findByUserIdAndTagId(userId, tagId);
        if (profile == null) {
            throw new RuntimeException("태그 프로필을 찾을 수 없습니다: userId=" + userId + ", tagId=" + tagId);
        }

        return convertToResponse(profile);
    }

    public void resetUserTagProfiles(UUID userId) {
        log.info("사용자 태그 프로필 초기화: {}", userId);

        List<UserTagProfile> profiles = userTagProfileRepository.findByUserId(userId);
        for (UserTagProfile profile : profiles) {
            profile.setScore(0.0f);
            userTagProfileRepository.save(profile);
        }
    }

    private UserTagProfileResponse convertToResponse(UserTagProfile profile) {
        // 태그 정보 조회
        Tag tag = tagRepository.findById(profile.getId().getTagId())
                .orElseThrow(() -> new RuntimeException("태그를 찾을 수 없습니다: " + profile.getId().getTagId()));

        return UserTagProfileResponse.builder()
                .userId(profile.getId().getUserId())
                .tagId(profile.getId().getTagId())
                .tagName(tag.getName())
                .score(profile.getScore())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}