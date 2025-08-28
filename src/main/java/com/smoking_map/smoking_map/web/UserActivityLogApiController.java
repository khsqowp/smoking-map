// src/main/java/com/smoking_map/smoking_map/web/UserActivityLogApiController.java

package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.service.UserActivityLogService;
import com.smoking_map.smoking_map.web.dto.UserActivityLogRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserActivityLogApiController {

    private final UserActivityLogService userActivityLogService;

    @PostMapping("/api/v1/activity-log")
    public ResponseEntity<Void> logActivity(@RequestBody UserActivityLogRequestDto requestDto) {
        userActivityLogService.logActivity(requestDto);
        return ResponseEntity.ok().build();
    }
}