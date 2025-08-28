package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.place.Place;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AdminPlaceDetailDto {
    private final Long id;
    private final String roadAddress;
    private final String description;
    private final String creatorEmail;
    private final List<AdminEditRequestDto> editRequests;
    private final List<AdminReviewDto> reviews; // --- ▼▼▼ [추가] 관리자용 리뷰 목록 필드 ▼▼▼ ---

    public AdminPlaceDetailDto(Place place) {
        this.id = place.getId();
        this.roadAddress = place.getRoadAddress();
        this.description = place.getDescription();
        this.creatorEmail = (place.getUser() != null) ? place.getUser().getEmail() : "N/A";
        this.editRequests = place.getEditRequests().stream()
                .map(AdminEditRequestDto::new)
                .collect(Collectors.toList());
        // --- ▼▼▼ [추가] 리뷰 목록을 AdminReviewDto로 변환하여 할당 ▼▼▼ ---
        this.reviews = place.getReviews().stream()
                .map(AdminReviewDto::new)
                .collect(Collectors.toList());
    }
}