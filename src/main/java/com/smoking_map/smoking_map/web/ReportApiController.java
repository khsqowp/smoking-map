// src/main/java/com/smoking_map/smoking_map/web/ReportApiController.java
package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.service.ReportService;
import com.smoking_map.smoking_map.web.dto.ReportRequestDto;
import jakarta.validation.Valid; // --- ▼▼▼ [수정] import 추가 ▼▼▼ ---
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;

    @PostMapping("/api/v1/places/{placeId}/report")
    public ResponseEntity<Void> submitReport(@PathVariable Long placeId,
                                             @Valid @RequestBody ReportRequestDto requestDto) { // --- ▼▼▼ [수정] @Valid 추가 ▼▼▼ ---
        reportService.submitReport(placeId, requestDto);
        return ResponseEntity.ok().build();
    }
}