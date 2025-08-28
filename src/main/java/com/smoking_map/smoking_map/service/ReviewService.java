package com.smoking_map.smoking_map.service;

import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.review.Review;
import com.smoking_map.smoking_map.domain.review.ReviewRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.ReviewRequestDto;
import com.smoking_map.smoking_map.web.dto.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public Long createReview(String userEmail, Long placeId, ReviewRequestDto requestDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found"));

        Review review = Review.builder()
                .user(user)
                .place(place)
                .rating(requestDto.getRating())
                .comment(requestDto.getComment())
                .build();

        reviewRepository.save(review);
        updatePlaceReviewStats(place);

        return review.getId();
    }

    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getUser().equals(user)) {
            throw new IllegalStateException("Not authorized to delete this review");
        }

        Place place = review.getPlace();
        reviewRepository.delete(review);
        updatePlaceReviewStats(place);
    }

    // --- ▼▼▼ [추가] 특정 장소의 리뷰 목록을 가져오는 메서드 ▼▼▼ ---
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsForPlace(Long placeId, String userEmail) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found"));

        List<Review> reviews = reviewRepository.findByPlace(place);

        return reviews.stream()
                .map(review -> new ReviewResponseDto(
                        review,
                        userEmail != null && review.getUser().getEmail().equals(userEmail)
                ))
                .collect(Collectors.toList());
    }

    // This is a helper method to update review statistics
    private void updatePlaceReviewStats(Place place) {
        // --- ▼▼▼ [수정] 분리된 쿼리를 사용하여 통계 업데이트 ▼▼▼ ---

        // 1. 평균 평점 조회
        Double averageRating = reviewRepository.findAverageRatingByPlace(place);

        // 2. 리뷰 수 조회
        Long reviewCount = reviewRepository.countByPlace(place);

        // 3. Place 엔티티 업데이트 (null일 경우 기본값 0으로 처리)
        place.updateReviewStats(
                averageRating != null ? averageRating : 0.0,
                reviewCount != null ? reviewCount.intValue() : 0
        );
        // --- ▲▲▲ [수정] 분리된 쿼리를 사용하여 통계 업데이트 ▲▲▲ ---
    }
}