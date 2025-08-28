package com.smoking_map.smoking_map.web.dto.admin;

import com.smoking_map.smoking_map.domain.review.Review;
import lombok.Getter;
import java.time.format.DateTimeFormatter;

@Getter
public class AdminReviewDto {
    private final Long id;
    private final String userEmail;
    private final int rating;
    private final String comment;
    private final String createdAt;

    public AdminReviewDto(Review review) {
        this.id = review.getId();
        this.userEmail = review.getUser().getEmail();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}