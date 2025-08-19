package com.smoking_map.smoking_map.config.auth.dto;

import com.smoking_map.smoking_map.domain.user.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private final String name;
    private final String email;
    private final String picture;
    private final String role; // [확인] 이 role 필드가 있어야 합니다.

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
        this.role = user.getRole().name(); // User 엔티티의 Role을 문자열로 변환하여 저장
    }
}