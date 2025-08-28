package com.smoking_map.smoking_map.web.dto;

import com.smoking_map.smoking_map.domain.announcement.Announcement;
import lombok.Getter;

@Getter
public class AnnouncementResponseDto {
    private final Long id;
    private final String title;
    private final String content;

    public AnnouncementResponseDto(Announcement entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
    }
}