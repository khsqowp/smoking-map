package com.smoking_map.smoking_map.domain.announcement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List; // --- ▼▼▼ [수정] Optional -> List ▼▼▼ ---
import java.util.Optional;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // 현재 시간에 활성화되어 있는 공지 중 가장 최근에 생성된 것을 찾는 쿼리
    @Query("SELECT a FROM Announcement a WHERE a.active = true AND a.startDate <= :now AND a.endDate >= :now ORDER BY a.createdAt DESC")
    List<Announcement> findActiveAnnouncements(@Param("now") LocalDateTime now); // --- ▼▼▼ [수정] 메서드명 및 반환 타입 변경 ▼▼▼ ---
}