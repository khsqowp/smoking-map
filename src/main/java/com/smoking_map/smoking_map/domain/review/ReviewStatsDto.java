package com.smoking_map.smoking_map.domain.review;

import lombok.Getter;

@Getter
public class ReviewStatsDto {
    private final Double averageRating;
    private final Long reviewCount;

    public ReviewStatsDto(Double averageRating, Long reviewCount) {
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;
    }
}