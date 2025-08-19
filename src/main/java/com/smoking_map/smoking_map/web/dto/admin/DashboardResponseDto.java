package com.smoking_map.smoking_map.web.dto.admin;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class DashboardResponseDto {
    private final long totalPlaces;
    private final long totalUsers;
    private final long todayPlacesCount;
    private final List<RecentPlaceDto> recentPlaces;
    private final List<RecentUserDto> recentUsers;

    @Builder
    public DashboardResponseDto(long totalPlaces, long totalUsers, long todayPlacesCount, List<RecentPlaceDto> recentPlaces, List<RecentUserDto> recentUsers) {
        this.totalPlaces = totalPlaces;
        this.totalUsers = totalUsers;
        this.todayPlacesCount = todayPlacesCount;
        this.recentPlaces = recentPlaces;
        this.recentUsers = recentUsers;
    }
}