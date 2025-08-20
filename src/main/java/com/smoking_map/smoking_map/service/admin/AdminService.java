package com.smoking_map.smoking_map.service.admin;

import com.smoking_map.smoking_map.domain.edit_request.EditRequest;
import com.smoking_map.smoking_map.domain.edit_request.EditRequestRepository;
import com.smoking_map.smoking_map.domain.edit_request.RequestStatus;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.report.ReportRepository;
import com.smoking_map.smoking_map.domain.user.Role;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.admin.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.smoking_map.smoking_map.domain.report.Report; // import 추가
import com.smoking_map.smoking_map.domain.report.ReportType; // import 추가
import java.util.stream.Collectors; // import 추가
import java.util.Map; // import 추가

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final EditRequestRepository editRequestRepository;


    public DashboardResponseDto getDashboardData(String range) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentPeriodStart;
        LocalDateTime prevPeriodStart;
        LocalDateTime prevPeriodEnd;

        switch (range.toLowerCase()) {
            case "monthly":
                currentPeriodStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
                prevPeriodStart = currentPeriodStart.minusMonths(1);
                prevPeriodEnd = currentPeriodStart.minusNanos(1);
                break;
            case "yearly":
                currentPeriodStart = now.withDayOfYear(1).toLocalDate().atStartOfDay();
                prevPeriodStart = currentPeriodStart.minusYears(1);
                prevPeriodEnd = currentPeriodStart.minusNanos(1);
                break;
            case "weekly":
            default:
                currentPeriodStart = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
                prevPeriodStart = currentPeriodStart.minusWeeks(1);
                prevPeriodEnd = currentPeriodStart.minusNanos(1);
                break;
        }

        long totalPlaces = placeRepository.count();
        long totalUsers = userRepository.count();

        long currentPlaces = placeRepository.countByCreatedAtBetween(currentPeriodStart, now);
        long prevPlaces = placeRepository.countByCreatedAtBetween(prevPeriodStart, prevPeriodEnd);

        long currentUsers = userRepository.countByCreatedAtBetween(currentPeriodStart, now);
        long prevUsers = userRepository.countByCreatedAtBetween(prevPeriodStart, prevPeriodEnd);

        double placesGrowthRate = (prevPlaces == 0) ? (currentPlaces > 0 ? 100.0 : 0.0) : ((double) (currentPlaces - prevPlaces) / prevPlaces) * 100;
        double usersGrowthRate = (prevUsers == 0) ? (currentUsers > 0 ? 100.0 : 0.0) : ((double) (currentUsers - prevUsers) / prevUsers) * 100;

        // (예시) 차트 데이터 생성 - 실제 구현 시에는 더 상세한 로직 필요
        Map<String, Long> newPlacesChartData = Map.of(range, currentPlaces);
        Map<String, Long> newUsersChartData = Map.of(range, currentUsers);

        return DashboardResponseDto.builder()
                .totalPlaces(totalPlaces)
                .totalUsers(totalUsers)
                .periodPlacesCount(currentPlaces)
                .placesGrowthRate(placesGrowthRate)
                .usersGrowthRate(usersGrowthRate)
                .newPlacesChartData(newPlacesChartData)
                .newUsersChartData(newUsersChartData)
                .build();
    }

    public List<AdminPlaceDto> getAllPlaces(String searchTerm) {
        List<Place> places;
        if (StringUtils.hasText(searchTerm)) {
            places = placeRepository.findByRoadAddressContaining(searchTerm);
        } else {
            places = placeRepository.findAll();
        }

        // 모든 PENDING 상태의 요청을 가져와 장소 ID별로 그룹화하여 개수 계산
        Map<Long, Long> pendingCounts = editRequestRepository.findAllByStatus(RequestStatus.PENDING)
                .stream()
                .collect(Collectors.groupingBy(
                        request -> request.getPlace().getId(),
                        Collectors.counting()
                ));

        return places.stream()
                .map(place -> new AdminPlaceDto(place, pendingCounts.getOrDefault(place.getId(), 0L).intValue()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePlaceDescription(Long placeId, PlaceUpdateRequestDto requestDto) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        place.updateDescription(requestDto.getDescription());

        // 수정 완료 후, 해당 장소의 PENDING 상태인 모든 요청을 REVIEWED로 변경
        List<EditRequest> pendingRequests = editRequestRepository.findAllByPlaceAndStatus(place, RequestStatus.PENDING);
        pendingRequests.forEach(request -> request.updateStatus(RequestStatus.REVIEWED));
    }

    @Transactional
    public void deletePlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        placeRepository.delete(place);
    }

    public List<String> getPlaceImageUrls(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));
        return place.getImageUrls();
    }

    public List<AdminReportDto> getAllReports() {
        return reportRepository.findAll().stream()
                .map(AdminReportDto::new)
                .collect(Collectors.toList());
    }

    public List<UserContributionDto> getUserContributions() {
        List<Object[]> results = userRepository.countPlacesByUser();
        return results.stream()
                .map(result -> new UserContributionDto((User) result[0], (Long) result[1]))
                .sorted(Comparator.comparing(UserContributionDto::getPlaceCount).reversed())
                .collect(Collectors.toList());
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

    public StatsResponseDto getStatsData() {
        // 각 기간의 시작점 계산
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = todayStart.with(DayOfWeek.MONDAY);
        LocalDateTime monthStart = todayStart.withDayOfMonth(1);
        LocalDateTime yearStart = todayStart.withDayOfYear(1);

        // 각 기간별 장소 등록 수 계산
        long placesDaily = placeRepository.countByCreatedAtAfter(todayStart);
        long placesWeekly = placeRepository.countByCreatedAtAfter(weekStart);
        long placesMonthly = placeRepository.countByCreatedAtAfter(monthStart);
        long placesYearly = placeRepository.countByCreatedAtAfter(yearStart);

        // 각 기간별 사용자 가입 수 계산 (2단계에서 추가한 메서드 사용)
        long usersDaily = userRepository.countByCreatedAtAfter(todayStart);
        long usersWeekly = userRepository.countByCreatedAtAfter(weekStart);
        long usersMonthly = userRepository.countByCreatedAtAfter(monthStart);
        long usersYearly = userRepository.countByCreatedAtAfter(yearStart);

        // 1단계에서 만든 DTO로 결과를 조립하여 반환
        return StatsResponseDto.builder()
                .placesDaily(placesDaily)
                .placesWeekly(placesWeekly)
                .placesMonthly(placesMonthly)
                .placesYearly(placesYearly)
                .usersDaily(usersDaily)
                .usersWeekly(usersWeekly)
                .usersMonthly(usersMonthly)
                .usersYearly(usersYearly)
                .build();
    }

    public List<GroupedAdminReportDto> getGroupedReports() {
        // 모든 신고를 장소 ID(place.id)로 그룹화
        Map<Place, List<Report>> groupedByPlace = reportRepository.findAll().stream()
                .collect(Collectors.groupingBy(Report::getPlace));

        // 그룹화된 데이터를 새로운 DTO로 변환
        return groupedByPlace.entrySet().stream()
                .map(entry -> {
                    Place place = entry.getKey();
                    List<Report> reports = entry.getValue();

                    // 신고 유형별 횟수 계산
                    Map<String, Long> typeCounts = reports.stream()
                            .collect(Collectors.groupingBy(
                                    report -> report.getType().getDescription(),
                                    Collectors.counting()
                            ));

                    // '기타' 유형의 내용만 필터링하여 리스트로 만듦
                    List<String> otherContents = reports.stream()
                            .filter(report -> report.getType() == ReportType.OTHER)
                            .map(Report::getContent)
                            .collect(Collectors.toList());

                    return GroupedAdminReportDto.builder()
                            .placeId(place.getId())
                            .roadAddress(place.getRoadAddress())
                            .reportTypeCounts(typeCounts)
                            .otherContents(otherContents)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<AdminEditRequestDto> getEditRequestsForPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        return editRequestRepository.findAllByPlaceAndStatus(place, RequestStatus.PENDING)
                .stream()
                .map(AdminEditRequestDto::new)
                .collect(Collectors.toList());
    }
}