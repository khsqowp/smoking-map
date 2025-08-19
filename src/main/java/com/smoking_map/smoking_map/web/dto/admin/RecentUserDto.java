package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.user.User;
import lombok.Getter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class RecentUserDto {
    private final Long id;
    private final String name;
    private final String email;
    private final String createdAt;

    public RecentUserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();

        // [수정된 부분] createdAt이 null인지 확인하는 로직 추가
        LocalDateTime createdAtValue = user.getCreatedAt();
        if (createdAtValue != null) {
            this.createdAt = createdAtValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } else {
            this.createdAt = "N/A";
        }
    }
}