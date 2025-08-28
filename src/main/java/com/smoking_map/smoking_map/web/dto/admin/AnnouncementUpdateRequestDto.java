package com.smoking_map.smoking_map.web.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AnnouncementUpdateRequestDto {
    private String title;
    private String content;
    private boolean active;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}