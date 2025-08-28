package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.announcement.Announcement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AnnouncementSaveRequestDto {
    private String title;
    private String content;
    private boolean active;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Announcement toEntity() {
        return Announcement.builder()
                .title(title)
                .content(content)
                .active(active)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}