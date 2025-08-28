package com.smoking_map.smoking_map.domain.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // --- ▼▼▼ [추가] import ▼▼▼ ---

public interface PlaceRepository extends JpaRepository<Place, Long> {
    long countByCreatedAtAfter(LocalDateTime startOfDay);
    List<Place> findTop5ByOrderByIdDesc();
    List<Place> findByRoadAddressContaining(String roadAddress);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM Place p WHERE p.roadAddress LIKE %:keyword% OR p.originalAddress LIKE %:keyword%")
    List<Place> findByAddressKeyword(@Param("keyword") String keyword);

    long countByCreatedAtBefore(LocalDateTime time);
    List<Place> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // --- ▼▼▼ [추가] 장소와 수정 제안 목록을 함께 조회하는 쿼리 ▼▼▼ ---
    @Query("SELECT p FROM Place p LEFT JOIN FETCH p.editRequests WHERE p.id = :id")
    Optional<Place> findByIdWithEditRequests(@Param("id") Long id);
}