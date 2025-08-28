package com.smoking_map.smoking_map.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDto {

    @Min(1)
    @Max(5)
    private int rating;

    @Size(max = 500)
    private String comment;
}