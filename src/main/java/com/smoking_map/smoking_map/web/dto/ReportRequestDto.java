package com.smoking_map.smoking_map.web.dto;

import com.smoking_map.smoking_map.domain.report.ReportType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequestDto {
    private ReportType type;
    private String content;
}