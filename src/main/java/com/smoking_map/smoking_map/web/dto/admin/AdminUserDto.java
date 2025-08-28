// src/main/java/com/smoking_map/smoking_map/web/dto/admin/AdminUserDto.java
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
    private final String picture; // --- ▼▼▼ [수정] picture 필드 추가 ▼▼▼ ---
    private final String role;
    private final String createdAt;

    public AdminUserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture(); // --- ▼▼▼ [수정] 생성자에 picture 할당 추가 ▼▼▼ ---
        this.role = user.getRole().name();

        LocalDateTime createdAtValue = user.getCreatedAt();
        if (createdAtValue != null) {
            this.createdAt = createdAtValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } else {
            this.createdAt = "N/A";
        }
    }
}