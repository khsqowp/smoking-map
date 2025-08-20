package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.user.User;
import lombok.Getter;

@Getter
public class UserContributionDto {
    private final Long userId;
    private final String name;
    private final String email;
    private final long placeCount;

    public UserContributionDto(User user, long placeCount) {
        this.userId = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.placeCount = placeCount;
    }
}