package com.smoking_map.smoking_map.service;

import com.smoking_map.smoking_map.domain.announcement.Announcement;
import com.smoking_map.smoking_map.domain.announcement.AnnouncementRepository;
import com.smoking_map.smoking_map.web.dto.AnnouncementResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; // --- ▼▼▼ [추가] import ▼▼▼ ---

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    // --- ▼▼▼ [수정] 활성화된 모든 공지를 리스트로 반환하도록 변경 ▼▼▼ ---
    public List<AnnouncementResponseDto> getActiveAnnouncements() {
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now())
                .stream()
                .map(AnnouncementResponseDto::new)
                .collect(Collectors.toList());
    }
    // --- ▲▲▲ [수정] 활성화된 모든 공지를 리스트로 반환하도록 변경 ▲▲▲ ---
}