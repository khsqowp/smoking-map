package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.place.Place;
import lombok.Getter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class RecentPlaceDto {
    private final Long id;
    private final String roadAddress;
    private final String createdAt;

    public RecentPlaceDto(Place place) {
        this.id = place.getId();
        this.roadAddress = place.getRoadAddress();

        // [수정된 부분] createdAt이 null인지 확인하는 로직 추가
        LocalDateTime createdAtValue = place.getCreatedAt();
        if (createdAtValue != null) {
            this.createdAt = createdAtValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } else {
            this.createdAt = "N/A"; // 또는 null이나 빈 문자열 등으로 처리
        }
    }
}