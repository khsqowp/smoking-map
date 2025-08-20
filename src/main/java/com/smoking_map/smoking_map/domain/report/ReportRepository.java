package com.smoking_map.smoking_map.domain.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 장소별 신고 횟수를 계산하기 위한 쿼리
    @Query("SELECT r.place.id, COUNT(r) FROM Report r GROUP BY r.place.id")
    List<Object[]> countReportsByPlace();

    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

}