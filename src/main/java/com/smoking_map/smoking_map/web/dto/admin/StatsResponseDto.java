// StatsResponseDto.java
package com.smoking_map.smoking_map.web.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsResponseDto {
    private final long placesDaily;
    private final long placesWeekly;
    private final long placesMonthly;
    private final long placesYearly;
    private final long usersDaily;
    private final long usersWeekly;
    private final long usersMonthly;
    private final long usersYearly;
}