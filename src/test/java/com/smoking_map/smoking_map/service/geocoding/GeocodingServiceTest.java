package com.smoking_map.smoking_map.service.geocoding;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // @Value 임포트
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class GeocodingServiceTest {

    @Autowired
    private GeocodingService geocodingService;

    // --- [디버깅 코드 추가] ---
    // 테스트 환경에 주입된 naver api key 값을 직접 확인하기 위한 필드
    @Value("${naver.api.client-id}")
    private String testClientId;

    @Value("${naver.api.client-secret}")
    private String testClientSecret;
    // --- [여기까지] ---

    @Test
    @DisplayName("특정 위도, 경도 좌표로 실제 Naver API를 호출하여 도로명 주소를 정상적으로 받아온다.")
    void getAddressFromCoords_IntegrationTest() {
        // 1. 준비 (Arrange)
        double latitude = 37.475998;
        double longitude = 126.977818;

        // --- [디버깅 코드 추가] ---
        // 테스트 시작 시, 주입된 API 키 값을 로그로 출력
        log.info("==================================================");
        log.info("[디버깅] Loaded Client ID: {}", testClientId);
        log.info("[디버깅] Loaded Client Secret: {}", testClientSecret);
        log.info("==================================================");
        // --- [여기까지] ---

        // 2. 실행 (Act)
        GeocodingService.GeocodingResult result = geocodingService.getAddressFromCoords(latitude, longitude);

        // 3. 검증 (Assert)
        log.info("==================================================");
        log.info("[테스트] 변환된 주소: {}", result.getFullAddress());
        log.info("[테스트] 시/도: {}", result.getSido());
        log.info("[테스트] 시/군/구: {}", result.getSigungu());
        log.info("==================================================");

        assertThat(result).isNotNull();
        assertThat(result.getFullAddress()).isNotEqualTo("주소 정보 없음");
        assertThat(result.getSido()).isNotEmpty();
    }
}