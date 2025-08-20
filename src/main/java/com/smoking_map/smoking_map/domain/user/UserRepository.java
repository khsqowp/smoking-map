package com.smoking_map.smoking_map.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findTop5ByOrderByIdDesc();

    // [추가] 각 사용자가 등록한 장소 수를 계산
    @Query("SELECT p.user, COUNT(p) FROM Place p WHERE p.user IS NOT NULL GROUP BY p.user")
    List<Object[]> countPlacesByUser();

    long countByCreatedAtAfter(java.time.LocalDateTime startTime);

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

}