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

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDto> getDashboardData() {
        return ResponseEntity.ok(adminService.getDashboardData());
    }

    @GetMapping("/places")
    public ResponseEntity<List<AdminPlaceDto>> getAllPlaces(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getAllPlaces(search));
    }

    @GetMapping("/places/{id}/images")
    public ResponseEntity<List<String>> getPlaceImageUrls(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getPlaceImageUrls(id));
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

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @RequestBody UserRoleUpdateRequestDto requestDto) {
        adminService.updateUserRole(id, requestDto);
        return ResponseEntity.ok().build();
    }
}