// src/main/java/com/smoking_map/smoking_map/web/dto/ReportRequestDto.java
package com.smoking_map.smoking_map.web.dto;

import com.smoking_map.smoking_map.domain.report.ReportType;
import jakarta.validation.constraints.NotNull; // --- ▼▼▼ [수정] import 추가 ▼▼▼ ---
import jakarta.validation.constraints.Size;   // --- ▼▼▼ [수정] import 추가 ▼▼▼ ---
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequestDto {

    @NotNull(message = "신고 유형을 선택해야 합니다.") // --- ▼▼▼ [수정] 어노테이션 추가 ▼▼▼ ---
    private ReportType type;

    @Size(max = 1000, message = "기타 신고 내용은 1000자를 초과할 수 없습니다.") // --- ▼▼▼ [수정] 어노테이션 추가 ▼▼▼ ---
    private String content;
}