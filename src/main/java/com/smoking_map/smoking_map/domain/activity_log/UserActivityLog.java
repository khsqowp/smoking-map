// src/main/java/com/smoking_map/smoking_map/domain/activity_log/UserActivityLog.java

package com.smoking_map.smoking_map.domain.activity_log;

import com.smoking_map.smoking_map.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Entity
public class UserActivityLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;

    // 로그인한 사용자의 경우 ID를 저장
    @Column
    private Long userId;

    // 비로그인 사용자를 포함한 세션 식별자
    @Column(nullable = false)
    private String sessionId;

    @Builder
    public UserActivityLog(BigDecimal latitude, BigDecimal longitude, Long userId, String sessionId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
        this.sessionId = sessionId;
    }
}