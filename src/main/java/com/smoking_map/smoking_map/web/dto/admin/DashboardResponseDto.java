// DashboardResponseDto.java
package com.smoking_map.smoking_map.web.dto.admin;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashboardResponseDto {
    // 기존 필드
    private final long totalPlaces;
    private final long totalUsers;
    private final long periodPlacesCount; // 'today' -> 'period'로 이름 변경

    // 증감률 데이터 추가
    private final double placesGrowthRate;
    private final double usersGrowthRate;

    // 차트 데이터 추가 (Key: 날짜(String), Value: 수(Long))
    private final Map<String, Long> newPlacesChartData;
    private final Map<String, Long> newUsersChartData;
}