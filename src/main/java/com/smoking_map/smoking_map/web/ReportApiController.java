package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.service.ReportService;
import com.smoking_map.smoking_map.web.dto.ReportRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;

    @PostMapping("/api/v1/places/{placeId}/report")
    public ResponseEntity<Void> submitReport(@PathVariable Long placeId, @RequestBody ReportRequestDto requestDto) {
        reportService.submitReport(placeId, requestDto);
        return ResponseEntity.ok().build();
    }
}