package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.service.AnnouncementService;
import com.smoking_map.smoking_map.web.dto.AnnouncementResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List; // --- ▼▼▼ [추가] import ▼▼▼ ---

@RestController
@RequiredArgsConstructor
public class AnnouncementApiController {

    private final AnnouncementService announcementService;

    // --- ▼▼▼ [수정] List<DTO>를 반환하도록 변경 ▼▼▼ ---
    @GetMapping("/api/v1/announcements/active")
    public ResponseEntity<List<AnnouncementResponseDto>> getActiveAnnouncements() {
        return ResponseEntity.ok(announcementService.getActiveAnnouncements());
    }
    // --- ▲▲▲ [수정] List<DTO>를 반환하도록 변경 ▲▲▲ ---
}