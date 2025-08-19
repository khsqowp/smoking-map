package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.service.admin.AdminService;
import com.smoking_map.smoking_map.web.dto.admin.DashboardResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminApiController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDto> getDashboardData() {
        return ResponseEntity.ok(adminService.getDashboardData());
    }
}