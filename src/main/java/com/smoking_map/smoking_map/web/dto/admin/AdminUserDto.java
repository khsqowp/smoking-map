package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.user.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class AdminUserDto {
    private final Long id;
    private final String name;
    private final String email;
    private final String role;
    private final String createdAt;

    public AdminUserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().name();

        LocalDateTime createdAtValue = user.getCreatedAt();
        if (createdAtValue != null) {
            this.createdAt = createdAtValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } else {
            this.createdAt = "N/A";
        }
    }
}