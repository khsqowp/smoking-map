package com.smoking_map.smoking_map.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    // 스프링 시큐리티에서는 권한 코드에 항상 ROLE_ 접두사가 필요합니다.
    GUEST("ROLE_GUEST", "손님"),
    USER("ROLE_USER", "일반 사용자"),
    MANAGER("ROLE_MANAGER", "매니저"), // MANAGER 역할 추가
    ADMIN("ROLE_ADMIN", "관리자");     // ADMIN 역할 추가

    private final String key;
    private final String title;
}