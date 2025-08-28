package com.smoking_map.smoking_map.web.dto;

import com.smoking_map.smoking_map.domain.place.Place;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class PlaceResponseDto {
    private Long id;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String originalAddress;
    private String roadAddress;
    private String description;
    private List<String> imageUrls;
    private boolean isFavorited;
    // --- ▼▼▼ [추가] 평점 및 리뷰 수 필드 ▼▼▼ ---
    private double averageRating;
    private int reviewCount;

    public PlaceResponseDto(Place entity) {
        this.id = entity.getId();
        this.latitude = entity.getLatitude();
        this.longitude = entity.getLongitude();
        this.originalAddress = entity.getOriginalAddress();
        this.roadAddress = entity.getRoadAddress();
        this.description = entity.getDescription();
        this.imageUrls = entity.getImageUrls();
        this.isFavorited = false;
        // --- ▼▼▼ [추가] 평점 정보 초기화 ▼▼▼ ---
        this.averageRating = entity.getAverageRating();
        this.reviewCount = entity.getReviewCount();
    }

    // --- ▼▼▼ [추가] 즐겨찾기 여부를 받는 생성자 오버로딩 ▼▼▼ ---
    public PlaceResponseDto(Place entity, boolean isFavorited) {
        this.id = entity.getId();
        this.latitude = entity.getLatitude();
        this.longitude = entity.getLongitude();
        this.originalAddress = entity.getOriginalAddress();
        this.roadAddress = entity.getRoadAddress();
        this.description = entity.getDescription();
        this.imageUrls = entity.getImageUrls();
        this.isFavorited = isFavorited;
        // --- ▼▼▼ [추가] 평점 정보 초기화 ▼▼▼ ---
        this.averageRating = entity.getAverageRating();
        this.reviewCount = entity.getReviewCount();
    }
}