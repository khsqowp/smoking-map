package com.smoking_map.smoking_map.web.dto;

import com.smoking_map.smoking_map.domain.review.Review;
import lombok.Getter;
import java.time.format.DateTimeFormatter;

@Getter
public class ReviewResponseDto {
    private final Long id;
    private final String userName;
    private final String userPicture;
    private final int rating;
    private final String comment;
    private final String createdAt;
    private final boolean writtenByCurrentUser;

    public ReviewResponseDto(Review review, boolean writtenByCurrentUser) {
        this.id = review.getId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.writtenByCurrentUser = writtenByCurrentUser;

        // --- ▼▼▼ [수정] 익명 처리 로직 복원 ▼▼▼ ---
        if (writtenByCurrentUser) {
            this.userName = review.getUser().getName();
            this.userPicture = review.getUser().getPicture();
        } else {
            this.userName = "익명";
            // 깨진 이미지를 유발했던 경로. 프론트엔드에서 이 경로를 사용하지 않도록 할 것입니다.
            this.userPicture = "/anonymous-avatar.png";
        }
        // --- ▲▲▲ [수정] 익명 처리 로직 복원 ▲▲▲ ---
    }
}