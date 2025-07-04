package com.couple.question_answer.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.question_answer.entity.Tag;
import com.couple.question_answer.repository.TagRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag", description = "태그 관련 API")
public class TagController {

    private final TagRepository tagRepository;

    @GetMapping
    @Operation(summary = "전체 태그 조회", description = "모든 태그를 조회합니다.")
    public ResponseEntity<ApiResponse<List<Tag>>> getAllTags() {
        log.info("전체 태그 조회 요청");

        List<Tag> tags = tagRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("태그 목록 조회 성공", tags));
    }
}