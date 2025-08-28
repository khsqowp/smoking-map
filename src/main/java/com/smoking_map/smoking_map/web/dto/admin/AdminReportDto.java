package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.report.Report;
import lombok.Getter;

@Getter
public class AdminReportDto {
    private final Long reportId;
    private final Long placeId;
    private final String roadAddress;
    private final String reportType;
    private final String content;
    private final String reporterEmail;

    public AdminReportDto(Report report) {
        this.reportId = report.getId();
        this.placeId = report.getPlace().getId();
        this.roadAddress = report.getPlace().getRoadAddress();
        this.reportType = report.getType().getDescription();
        this.content = report.getContent();
        this.reporterEmail = (report.getUser() != null) ? report.getUser().getEmail() : "비로그인 사용자";
    }
}