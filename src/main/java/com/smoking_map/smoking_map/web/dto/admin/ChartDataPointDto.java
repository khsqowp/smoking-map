package com.smoking_map.smoking_map.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChartDataPointDto {
    private String label;           // X축 레이블 (예: "08-01", "8월 1주차")
    private long newPlaces;         // 신규 장소 수
    private long newUsers;          // 신규 가입자 수
    private long totalPlaces;       // 누적 장소 수
    private long totalUsers;        // 누적 가입자 수
}