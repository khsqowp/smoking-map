package com.smoking_map.smoking_map.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime; // --- ▼▼▼ [추가] import ▼▼▼ ---
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findTop5ByOrderByIdDesc();

    @Query("SELECT p.user, COUNT(p) FROM Place p WHERE p.user IS NOT NULL GROUP BY p.user")
    List<Object[]> countPlacesByUser();

    long countByCreatedAtAfter(LocalDateTime startTime);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // --- ▼▼▼ [추가] 특정 시간 이전의 누적 개수 조회 ▼▼▼ ---
    long countByCreatedAtBefore(LocalDateTime time);

    // --- ▼▼▼ [추가] 특정 기간 동안의 모든 유저 조회 ▼▼▼ ---
    List<User> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}