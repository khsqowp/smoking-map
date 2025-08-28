package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.announcement.Announcement;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminAnnouncementDto {
    private final Long id;
    private final String title;
    private final String content;
    private final boolean active;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final LocalDateTime createdAt;

    public AdminAnnouncementDto(Announcement entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.active = entity.isActive();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.createdAt = entity.getCreatedAt();
    }
}