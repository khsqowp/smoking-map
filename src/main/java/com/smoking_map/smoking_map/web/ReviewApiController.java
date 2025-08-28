package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.config.auth.LoginUser;
import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.service.ReviewService;
import com.smoking_map.smoking_map.web.dto.ReviewRequestDto;
import com.smoking_map.smoking_map.web.dto.ReviewResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;

    @PostMapping("/api/v1/places/{placeId}/reviews")
    public ResponseEntity<Long> createReview(@PathVariable Long placeId,
                                             @LoginUser SessionUser user,
                                             @Valid @RequestBody ReviewRequestDto requestDto) {
        if (user == null) return ResponseEntity.status(401).build();
        Long reviewId = reviewService.createReview(user.getEmail(), placeId, requestDto);
        return ResponseEntity.ok(reviewId);
    }

    @DeleteMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             @LoginUser SessionUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        reviewService.deleteReview(reviewId, user.getEmail());
        return ResponseEntity.ok().build();
    }

    // --- ▼▼▼ [추가] 리뷰 목록 조회 API 엔드포인트 ▼▼▼ ---
    @GetMapping("/api/v1/places/{placeId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(@PathVariable Long placeId,
                                                              @LoginUser SessionUser user) {
        String userEmail = (user != null) ? user.getEmail() : null;
        return ResponseEntity.ok(reviewService.getReviewsForPlace(placeId, userEmail));
    }
}