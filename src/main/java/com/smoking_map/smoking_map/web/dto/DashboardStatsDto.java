package com.smoking_map.smoking_map.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DashboardStatsDto {
    private final long totalPlaces;
    private final long totalUsers;
    // 필요에 따라 오늘 등록된 장소 수 등 추가 가능

    @Builder
    public DashboardStatsDto(long totalPlaces, long totalUsers) {
        this.totalPlaces = totalPlaces;
        this.totalUsers = totalUsers;
    }
}