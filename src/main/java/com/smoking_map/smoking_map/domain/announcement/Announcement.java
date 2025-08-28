package com.smoking_map.smoking_map.domain.announcement;

import com.smoking_map.smoking_map.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class Announcement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Builder
    public Announcement(String title, String content, boolean active, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.content = content;
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(String title, String content, boolean active, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.content = content;
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // --- ▼▼▼ [추가] 활성 상태를 토글하는 메서드 ▼▼▼ ---
    public void toggleActive() {
        this.active = !this.active;
    }
}