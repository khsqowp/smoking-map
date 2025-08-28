// src/main/java/com/smoking_map/smoking_map/web/dto/UserActivityLogRequestDto.java

package com.smoking_map.smoking_map.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class UserActivityLogRequestDto {
    private BigDecimal latitude;
    private BigDecimal longitude;
}