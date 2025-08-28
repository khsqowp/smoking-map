package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.place.ImageInfo;
import lombok.Getter;

@Getter
public class AdminImageDto {
    private final Long id;
    private final String url;
    private final boolean isRepresentative;

    public AdminImageDto(ImageInfo imageInfo) {
        this.id = imageInfo.getId();
        this.url = imageInfo.getImageUrl();
        this.isRepresentative = imageInfo.isRepresentative();
    }
}