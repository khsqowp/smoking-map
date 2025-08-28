package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.place.Place;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class AdminPlaceDto {
    private final Long id;
    private final String roadAddress;
    private final String description;
    private final int imageCount;
    private final String creatorEmail;
    private final String createdAt;
    private final int favoriteCount; // --- ▼▼▼ [추가] 즐겨찾기 수 필드 ▼▼▼ ---
    private final int reviewCount; // --- ▼▼▼ [추가] 리뷰 수 필드 ▼▼▼ ---
    private final int editRequestCount;

    public AdminPlaceDto(Place place, int favoriteCount, int reviewCount, int editRequestCount) {
        this.id = place.getId();
        this.roadAddress = place.getRoadAddress();
        this.description = place.getDescription();
        this.imageCount = place.getImageUrls().size();
        this.creatorEmail = (place.getUser() != null) ? place.getUser().getEmail() : "N/A";

        LocalDateTime createdAtValue = place.getCreatedAt();
        if (createdAtValue != null) {
            this.createdAt = createdAtValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } else {
            this.createdAt = "N/A";
        }

        this.favoriteCount = favoriteCount;
        this.reviewCount = reviewCount; // 이 라인이 정상적으로 동작하도록 수정됨
        this.editRequestCount = editRequestCount;
    }
}