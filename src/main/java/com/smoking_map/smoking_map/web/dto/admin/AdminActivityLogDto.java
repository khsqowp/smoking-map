// src/main/java/com/smoking_map/smoking_map/web/dto/admin/AdminActivityLogDto.java

package com.smoking_map.smoking_map.web.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AdminActivityLogDto {
    private final Long id;
    private final String activityTime;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String userType; // "로그인" 또는 "비회원"
    private final String identifier; // 사용자 이메일 또는 세션 ID
}