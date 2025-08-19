package com.smoking_map.smoking_map.service.admin;

import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.user.Role;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.admin.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    public List<AdminPlaceDto> getAllPlaces(String searchTerm) {
        List<Place> places;
        if (StringUtils.hasText(searchTerm)) {
            places = placeRepository.findByRoadAddressContaining(searchTerm);
        } else {
            places = placeRepository.findAll();
        }
        return places.stream()
                .map(AdminPlaceDto::new)
                .collect(Collectors.toList());
    }

    public List<String> getPlaceImageUrls(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));
        return place.getImageUrls();
    }

    @Transactional
    public void updatePlaceDescription(Long placeId, PlaceUpdateRequestDto requestDto) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        place.updateDescription(requestDto.getDescription());
    }

    @Transactional
    public void deletePlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        placeRepository.delete(place);
    }

    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserRole(Long userId, UserRoleUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + userId));

        Role newRole = Role.valueOf(requestDto.getRole());
        user.updateRole(newRole);
    }
}