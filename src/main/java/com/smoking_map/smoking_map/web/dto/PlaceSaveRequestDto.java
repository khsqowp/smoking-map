package com.smoking_map.smoking_map.web.dto;

import com.smoking_map.smoking_map.domain.place.Place;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PlaceSaveRequestDto {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String originalAddress;
    private String description;

    @Builder
    public PlaceSaveRequestDto(BigDecimal latitude, BigDecimal longitude, String originalAddress, String description) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.originalAddress = originalAddress;
        this.description = description;
    }

    public Place toEntity() {
        return Place.builder()
                .latitude(latitude)
                .longitude(longitude)
                .originalAddress(originalAddress)
                .description(description)
                .build();
    }
}