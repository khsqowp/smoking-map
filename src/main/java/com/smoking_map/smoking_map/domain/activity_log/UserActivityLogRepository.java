// src/main/java/com/smoking_map/smoking_map/domain/activity_log/UserActivityLogRepository.java

package com.smoking_map.smoking_map.domain.activity_log;

import org.springframework.data.jpa.repository.JpaRepository;
// --- ▼▼▼ [추가] import ▼▼▼ ---
import java.util.List;
// --- ▲▲▲ [추가] import ▲▲▲ ---

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    // --- ▼▼▼ [추가] ID 역순으로 최근 100개 조회 메서드 ▼▼▼ ---
    List<UserActivityLog> findTop100ByOrderByIdDesc();
    // --- ▲▲▲ [추가] ID 역순으로 최근 100개 조회 메서드 ▲▲▲ ---
}