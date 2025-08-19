package com.smoking_map.smoking_map.service.admin;

import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.admin.DashboardResponseDto;
import com.smoking_map.smoking_map.web.dto.admin.RecentPlaceDto;
import com.smoking_map.smoking_map.web.dto.admin.RecentUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    public DashboardResponseDto getDashboardData() {
        long totalPlaces = placeRepository.count();
        long totalUsers = userRepository.count();
        long todayPlacesCount = placeRepository.countByCreatedAtAfter(LocalDate.now().atStartOfDay());

        List<RecentPlaceDto> recentPlaces = placeRepository.findTop5ByOrderByIdDesc().stream()
                .map(RecentPlaceDto::new)
                .collect(Collectors.toList());

        List<RecentUserDto> recentUsers = userRepository.findTop5ByOrderByIdDesc().stream()
                .map(RecentUserDto::new)
                .collect(Collectors.toList());

        return DashboardResponseDto.builder()
                .totalPlaces(totalPlaces)
                .totalUsers(totalUsers)
                .todayPlacesCount(todayPlacesCount)
                .recentPlaces(recentPlaces)
                .recentUsers(recentUsers)
                .build();
    }
}