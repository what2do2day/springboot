package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.KakaoRegionResponse;
import com.couple.schedule_meeting.dto.PlaceRankResponse;
import com.couple.schedule_meeting.dto.PlaceResponse;
import com.couple.schedule_meeting.entity.Place;
import com.couple.schedule_meeting.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final KakaoApiService kakaoApiService;

    public Place getPlaceById(String placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place not found with id: " + placeId));
    }
    
    public List<PlaceRankResponse> getPlaceRanks(double lat, double lon, String code) {
        log.info("장소 랭킹 조회 시작 - lat: {}, lon: {}, code: {}", lat, lon, code);
        
        // Kakao API를 통해 좌표를 행정구역으로 변환
        String region = kakaoApiService.getRegionFromCoordinates(lon, lat)
                .map(response -> {
                    if (response.getDocuments() != null && !response.getDocuments().isEmpty()) {
                        KakaoRegionResponse.Document doc = response.getDocuments().get(0);
                        String region2depth = doc.getRegion2depthName(); // 구/군 단위 사용
                        log.info("Kakao API에서 추출한 행정구역: {}", region2depth);
                        return region2depth;
                    }
                    log.warn("Kakao API 응답에서 documents가 비어있습니다");
                    return null;
                })
                .block();
        
        if (region == null) {
            log.error("좌표에서 행정구역 정보를 가져오는데 실패했습니다");
            throw new RuntimeException("Failed to get region information from coordinates");
        }
        
        log.info("데이터베이스 조회 시작 - code: {}, region: {}", code, region);
        
        // 전체 장소 수 확인 (디버깅용)
        long totalPlaces = placeRepository.count();
        log.info("전체 장소 수: {}", totalPlaces);
        
        // code별 장소 수 확인 (디버깅용)
        List<Place> allPlaces = placeRepository.findAll();
        long codeCount = allPlaces.stream().filter(p -> p.getCode().equals(code)).count();
        log.info("code '{}'인 장소 수: {}", code, codeCount);
        
        // 실제 데이터베이스에 있는 code 값들 확인 (디버깅용)
        List<String> uniqueCodes = allPlaces.stream()
                .map(Place::getCode)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        log.info("데이터베이스에 있는 모든 code: {}", uniqueCodes);
        
        // code별 개수도 확인
        Map<String, Long> codeCounts = allPlaces.stream()
                .collect(Collectors.groupingBy(Place::getCode, Collectors.counting()));
        log.info("code별 장소 개수: {}", codeCounts);
        
        // 주소에 해당 지역이 포함된 장소 수 확인 (디버깅용)
        long regionCount = allPlaces.stream().filter(p -> p.getAddress().contains(region)).count();
        log.info("주소에 '{}'가 포함된 장소 수: {}", region, regionCount);
        
        // 해당 행정구역과 code로 장소 조회 (ID 순서로 정렬)
        List<Place> places = placeRepository.findByCodeAndAddressContainingOrderByIdAsc(code, region);
        
        // 상위 5개만 선택
        if (places.size() > 5) {
            places = places.subList(0, 5);
        }
        
        log.info("데이터베이스 조회 완료 - 조회된 장소 수: {}", places.size());
        
        List<PlaceRankResponse> responses = places.stream()
                .map(place -> {
                    PlaceRankResponse response = PlaceRankResponse.from(place);
                    log.debug("장소 정보 - id: {}, name: {}, address: {}, rating: {}, lat: {}, lon: {}", 
                            response.getId(), response.getName(), response.getAddress(), response.getRating(),
                            response.getLatitude(), response.getLongitude());
                    return response;
                })
                .collect(Collectors.toList());
        
        log.info("장소 랭킹 조회 완료 - 반환할 장소 수: {}", responses.size());
        return responses;
    }
} 