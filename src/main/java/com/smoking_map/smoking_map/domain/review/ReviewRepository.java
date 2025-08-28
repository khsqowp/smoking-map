package com.smoking_map.smoking_map.domain.review;

import com.smoking_map.smoking_map.domain.place.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPlace(Place place);

    // --- ▼▼▼ [수정] 쿼리를 두 개로 명확하게 분리 ▼▼▼ ---

    // 장소의 평균 평점을 Double 타입으로 직접 조회 (결과 없으면 null 반환)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.place = :place")
    Double findAverageRatingByPlace(@Param("place") Place place);

    // 장소의 리뷰 수를 Long 타입으로 직접 조회 (결과 없으면 0 반환)
    @Query("SELECT COUNT(r) FROM Review r WHERE r.place = :place")
    Long countByPlace(@Param("place") Place place);

    // --- ▲▲▲ [수정] 기존 getReviewStatsByPlace 메서드는 삭제 ▲▲▲ ---
    // --- ▼▼▼ [추가] 장소별 리뷰 수를 계산하는 쿼리 ▼▼▼ ---
    @Query("SELECT r.place.id, COUNT(r) FROM Review r GROUP BY r.place.id")
    List<Object[]> countReviewsByPlace();
}