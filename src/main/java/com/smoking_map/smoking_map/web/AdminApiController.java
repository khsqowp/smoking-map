package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.service.admin.AdminService;
import com.smoking_map.smoking_map.web.dto.admin.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminApiController {

    private final AdminService adminService;

    @GetMapping("/places/{id}")
    public ResponseEntity<AdminPlaceDetailDto> getPlaceDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getPlaceDetails(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDto> getDashboardData(@RequestParam(defaultValue = "weekly") String range) {
        return ResponseEntity.ok(adminService.getDashboardData(range));
    }

    @GetMapping("/places")
    public ResponseEntity<List<AdminPlaceDto>> getAllPlaces(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getAllPlaces(search));
    }

    @GetMapping("/places/{id}/images")
    public ResponseEntity<List<AdminImageDto>> getPlaceImages(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getPlaceImages(id));
    }

    @DeleteMapping("/places/{id}/images")
    public ResponseEntity<Void> deletePlaceImage(@PathVariable Long id, @RequestBody ImageDeleteRequestDto requestDto) {
        adminService.deletePlaceImage(id, requestDto.getImageUrl());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/places/{placeId}/images/{imageInfoId}/set-representative")
    public ResponseEntity<Void> setRepresentativeImage(@PathVariable Long placeId, @PathVariable Long imageInfoId) {
        adminService.setRepresentativeImage(placeId, imageInfoId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/places/{id}")
    public ResponseEntity<Void> updatePlace(@PathVariable Long id, @RequestBody PlaceUpdateRequestDto requestDto) {
        adminService.updatePlaceDescription(id, requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/places/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        adminService.deletePlace(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<List<AdminReportDto>> getAllReports() {
        return ResponseEntity.ok(adminService.getAllReports());
    }

    @GetMapping("/users/contributions")
    public ResponseEntity<List<UserContributionDto>> getUserContributions() {
        return ResponseEntity.ok(adminService.getUserContributions());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @RequestBody UserRoleUpdateRequestDto requestDto) {
        adminService.updateUserRole(id, requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponseDto> getStatsData() {
        return ResponseEntity.ok(adminService.getStatsData());
    }

    @GetMapping("/reports/grouped")
    public ResponseEntity<List<GroupedAdminReportDto>> getGroupedReports() {
        return ResponseEntity.ok(adminService.getGroupedReports());
    }

    @GetMapping("/places/{id}/edit-requests")
    public ResponseEntity<List<AdminEditRequestDto>> getEditRequestsForPlace(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getEditRequestsForPlace(id));
    }

    // --- ▼▼▼ [추가] 공지 관리 API 엔드포인트 ▼▼▼ ---
    @GetMapping("/announcements")
    public ResponseEntity<List<AdminAnnouncementDto>> getAllAnnouncements() {
        return ResponseEntity.ok(adminService.getAllAnnouncements());
    }

    @PostMapping("/announcements")
    public ResponseEntity<Long> createAnnouncement(@RequestBody AnnouncementSaveRequestDto requestDto) {
        return ResponseEntity.ok(adminService.createAnnouncement(requestDto));
    }

    @PutMapping("/announcements/{id}")
    public ResponseEntity<Void> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementUpdateRequestDto requestDto) {
        adminService.updateAnnouncement(id, requestDto);
        return ResponseEntity.ok().build();
    }


    // --- ▼▼▼ [추가] 공지 활성 상태 변경 API 엔드포인트 ▼▼▼ ---
    @PatchMapping("/announcements/{id}/toggle-active")
    public ResponseEntity<Void> toggleAnnouncementActive(@PathVariable Long id) {
        adminService.toggleAnnouncementActive(id);
        return ResponseEntity.ok().build();
    }
    // --- ▲▲▲ [추가] 공지 활성 상태 변경 API 엔드포인트 ▲▲▲ ---

    // --- ▼▼▼ [추가] 관리자 리뷰 삭제 API 엔드포인트 ▼▼▼ ---
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReviewByAdmin(@PathVariable Long reviewId) {
        adminService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        adminService.deleteAnnouncement(id);
        return ResponseEntity.ok().build();
    }

    // --- ▼▼▼ [추가] 대시보드 차트 데이터 API 엔드포인트 ▼▼▼ ---
    @GetMapping("/dashboard/chart")
    public ResponseEntity<DashboardChartResponseDto> getDashboardChartData(@RequestParam(defaultValue = "daily") String range) {
        return ResponseEntity.ok(adminService.getDashboardChartData(range));
    }

    @GetMapping("/activity-logs")
    public ResponseEntity<List<AdminActivityLogDto>> getActivityLogs() {
        return ResponseEntity.ok(adminService.getActivityLogs());
    }

    @GetMapping("/activity-logs/heatmap")
    public ResponseEntity<List<HeatmapDto>> getHeatmapData() {
        return ResponseEntity.ok(adminService.getHeatmapData());
    }

}