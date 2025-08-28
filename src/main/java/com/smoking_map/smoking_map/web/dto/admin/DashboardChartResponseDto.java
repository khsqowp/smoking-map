package com.smoking_map.smoking_map.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardChartResponseDto {
    private List<ChartDataPointDto> chartData;
}