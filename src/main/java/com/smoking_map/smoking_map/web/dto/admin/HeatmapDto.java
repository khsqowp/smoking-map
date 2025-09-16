package com.smoking_map.smoking_map.web.dto.admin;

import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class HeatmapDto {
    private final double latitude;
    private final double longitude;

    public HeatmapDto(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude.doubleValue();
        this.longitude = longitude.doubleValue();
    }
}
