package com.smoking_map.smoking_map.web.dto.admin;

import lombok.Getter;
import java.math.BigDecimal;
import java.io.Serializable;

@Getter
public class HeatmapDto {
    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public HeatmapDto(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
