package com.smoking_map.smoking_map.domain.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    DISAPPEARED("사라진 흡연구역"),
    INCORRECT("잘못 표기된 흡연구역"),
    OTHER("기타");

    private final String description;
}