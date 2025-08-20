// src/main/java/com/smoking_map/smoking_map/web/dto/admin/AdminPlaceDto.java
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
    private final int editRequestCount;

    public AdminPlaceDto(Place place, int editRequestCount) {
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
        this.editRequestCount = editRequestCount;
    }
}