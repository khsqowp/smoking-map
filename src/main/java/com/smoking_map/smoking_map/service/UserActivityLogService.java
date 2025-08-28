// src/main/java/com/smoking_map/smoking_map/service/UserActivityLogService.java

package com.smoking_map.smoking_map.service;

import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.domain.activity_log.UserActivityLog;
import com.smoking_map.smoking_map.domain.activity_log.UserActivityLogRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.UserActivityLogRequestDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserActivityLogService {

    private final UserActivityLogRepository userActivityLogRepository;
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Transactional
    public void logActivity(UserActivityLogRequestDto requestDto) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        Long userId = null;

        if (sessionUser != null) {
            // 실제 User 엔티티를 조회하여 ID를 가져옵니다.
            userId = userRepository.findByEmail(sessionUser.getEmail())
                    .map(User::getId)
                    .orElse(null);
        }

        String sessionId = httpSession.getId();

        UserActivityLog log = UserActivityLog.builder()
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .userId(userId)
                .sessionId(sessionId)
                .build();

        userActivityLogRepository.save(log);
    }
}