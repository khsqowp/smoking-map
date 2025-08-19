package com.smoking_map.smoking_map.web.dto;

import com.smoking_map.smoking_map.domain.place.Place;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class PlaceResponseDto {
    private Long id;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String originalAddress;
    private String roadAddress;
    private String description;
    private List<String> imageUrls;

    public PlaceResponseDto(Place entity) {
        this.id = entity.getId();
        this.latitude = entity.getLatitude();
        this.longitude = entity.getLongitude();
        this.originalAddress = entity.getOriginalAddress();
        this.roadAddress = entity.getRoadAddress();
        this.description = entity.getDescription();
        this.imageUrls = entity.getImageUrls();
    }
}