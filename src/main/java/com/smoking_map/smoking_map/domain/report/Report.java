package com.smoking_map.smoking_map.domain.report;

import com.smoking_map.smoking_map.domain.BaseTimeEntity;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // 비로그인 사용자도 신고할 수 있으므로 User는 nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public Report(Place place, User user, ReportType type, String content) {
        this.place = place;
        this.user = user;
        this.type = type;
        this.content = content;
    }
}