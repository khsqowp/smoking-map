package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class IndexController {

    private final HttpSession httpSession;

    @Value("${naver.api.client-id}")
    private String naverClientId;

    @Value("${naver.api.client-secret}")
    private String naverClientSecret;

    @GetMapping("/")
    public String index() {
        return "Login successful! Check the actual service at http://localhost:3000.";
    }

    @GetMapping("/api/v1/user")
    public ResponseEntity<SessionUser> getUser() {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/api/v1/geocode")
    public ResponseEntity<Map<String, String>> getRoadAddress(@RequestParam double lat, @RequestParam double lng) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc";

        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("coords", lng + "," + lat)
                .queryParam("output", "json")
                .queryParam("orders", "roadaddr")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", naverClientId);
        headers.set("X-NCP-APIGW-API-KEY", naverClientSecret);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
                if (!results.isEmpty()) {
                    Map<String, Object> region = (Map<String, Object>) results.get(0).get("region");
                    Map<String, Object> land = (Map<String, Object>) results.get(0).get("land");

                    String area1 = (String) ((Map<String, Object>) region.get("area1")).get("name");
                    String area2 = (String) ((Map<String, Object>) region.get("area2")).get("name");
                    String name = (String) land.get("name");
                    String number1 = (String) land.get("number1");

                    String fullAddress = String.format("%s %s %s %s", area1, area2, name, number1).trim();

                    return ResponseEntity.ok(Map.of("address", fullAddress));
                }
            }
            return ResponseEntity.ok(Map.of("address", "Address not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("address", "Failed to convert address"));
        }
    }
}