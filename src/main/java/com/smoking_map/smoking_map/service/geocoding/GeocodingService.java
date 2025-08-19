package com.smoking_map.smoking_map.service.geocoding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class GeocodingService {

    @Value("${naver.api.client-id}")
    private String naverClientId;

    @Value("${naver.api.client-secret}")
    private String naverClientSecret;

    public GeocodingResult getAddressFromCoords(double lat, double lng) {

        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc";

        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("coords", lng + "," + lat)
                .queryParam("output", "json")
                .queryParam("orders", "roadaddr")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ncp-apigw-api-key-id", naverClientId);
        headers.set("x-ncp-apigw-api-key", naverClientSecret);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
                if (!results.isEmpty()) {
                    Map<String, Object> roadAddrResult = results.get(0);
                    Map<String, Object> region = (Map<String, Object>) roadAddrResult.get("region");
                    Map<String, Object> land = (Map<String, Object>) roadAddrResult.get("land");
                    String sido = Optional.ofNullable(region).map(r -> (Map<String, Object>) r.get("area1")).map(a -> (String) a.get("name")).orElse("");
                    String sigungu = Optional.ofNullable(region).map(r -> (Map<String, Object>) r.get("area2")).map(a -> (String) a.get("name")).orElse("");
                    String roadName = Optional.ofNullable(land).map(l -> (String) l.get("name")).orElse("");
                    String buildingNumber = Optional.ofNullable(land).map(l -> (String) l.get("number1")).orElse("");
                    if (sigungu.isBlank()) { sigungu = sido; }
                    String fullAddress = String.format("%s %s %s %s", sido, sigungu, roadName, buildingNumber).trim();
                    log.info("좌표 ({}, {}) -> 주소 변환 성공: {}", lat, lng, fullAddress);
                    return new GeocodingResult(fullAddress, sido, sigungu);
                }
            }
        } catch (Exception e) {
            log.error("Naver Geocoding API 호출 중 예외 발생: {}", e.getMessage());
            return new GeocodingResult("주소 정보 없음", "기타", "unknown");
        }
        log.warn("좌표 ({}, {})에 대한 주소 변환 결과가 없습니다.", lat, lng);
        return new GeocodingResult("주소 정보 없음", "기타", "unknown");
    }

    @Getter
    @AllArgsConstructor
    public static class GeocodingResult {
        private String fullAddress;
        private String sido;
        private String sigungu;
    }
}