package com.couple.schedule_meeting.tmp;

import com.couple.schedule_meeting.tmp.dto.TmpTestRequest;
import com.couple.schedule_meeting.tmp.dto.TmpTestResponse;
import com.couple.schedule_meeting.tmp.service.TmpTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedules/tmp")
@RequiredArgsConstructor
public class TmpTestController {

    private final TmpTestService tmpTestService;

    /**
     * 테스트용 텍스트를 MongoDB에 저장
     */
    @PostMapping("/save")
    public ResponseEntity<TmpTestResponse> saveTestData(@RequestBody TmpTestRequest request) {
        try {
            log.info("테스트 데이터 저장 요청: {}", request.getText());
            TmpTestResponse response = tmpTestService.saveTestData(request);
            log.info("테스트 데이터 저장 성공: id={}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트 데이터 저장 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 모든 테스트 데이터 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<TmpTestResponse>> getAllTestData() {
        try {
            log.info("모든 테스트 데이터 조회 요청");
            List<TmpTestResponse> response = tmpTestService.getAllTestData();
            log.info("테스트 데이터 조회 성공: {}개", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트 데이터 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ID로 특정 테스트 데이터 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<TmpTestResponse> getTestDataById(@PathVariable String id) {
        try {
            log.info("테스트 데이터 조회 요청: id={}", id);
            TmpTestResponse response = tmpTestService.getTestDataById(id);
            if (response != null) {
                log.info("테스트 데이터 조회 성공: id={}", id);
                return ResponseEntity.ok(response);
            } else {
                log.warn("테스트 데이터를 찾을 수 없음: id={}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("테스트 데이터 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 테스트 데이터 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestData(@PathVariable String id) {
        try {
            log.info("테스트 데이터 삭제 요청: id={}", id);
            boolean deleted = tmpTestService.deleteTestData(id);
            if (deleted) {
                log.info("테스트 데이터 삭제 성공: id={}", id);
                return ResponseEntity.ok().build();
            } else {
                log.warn("삭제할 테스트 데이터를 찾을 수 없음: id={}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("테스트 데이터 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 