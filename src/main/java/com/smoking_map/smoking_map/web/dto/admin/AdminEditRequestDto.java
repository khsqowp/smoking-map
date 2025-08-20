// src/main/java/com/smoking_map/smoking_map/web/dto/admin/AdminEditRequestDto.java
package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.edit_request.EditRequest;
import com.smoking_map.smoking_map.domain.user.User;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class AdminEditRequestDto {

    private final Long id;
    private final String content;
    private final String requesterEmail;
    private final String createdAt;

    public AdminEditRequestDto(EditRequest entity) {
        this.id = entity.getId();
        this.content = entity.getContent();

        User user = entity.getUser();
        this.requesterEmail = (user != null) ? user.getEmail() : "알 수 없음";

        this.createdAt = entity.getCreatedAt() != null ?
                entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
    }
}