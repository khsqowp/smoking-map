package com.smoking_map.smoking_map.service.admin;

import com.smoking_map.smoking_map.domain.announcement.Announcement;
import com.smoking_map.smoking_map.domain.activity_log.UserActivityLog;
import com.smoking_map.smoking_map.domain.activity_log.UserActivityLogRepository;
import com.smoking_map.smoking_map.domain.announcement.AnnouncementRepository;
import com.smoking_map.smoking_map.domain.edit_request.EditRequest;
import com.smoking_map.smoking_map.domain.edit_request.EditRequestRepository;
import com.smoking_map.smoking_map.domain.edit_request.RequestStatus;
import com.smoking_map.smoking_map.domain.favorite.FavoriteRepository;
import com.smoking_map.smoking_map.domain.place.ImageInfo;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.report.Report;
import com.smoking_map.smoking_map.domain.report.ReportRepository;
import com.smoking_map.smoking_map.domain.report.ReportType;
import com.smoking_map.smoking_map.domain.review.Review;
import com.smoking_map.smoking_map.domain.review.ReviewRepository;
import com.smoking_map.smoking_map.domain.user.Role;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.service.s3.S3Uploader;
import com.smoking_map.smoking_map.web.dto.admin.AdminPlaceDetailDto;
import com.smoking_map.smoking_map.web.dto.admin.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final EditRequestRepository editRequestRepository;
    private final S3Uploader s3Uploader;
    private final AnnouncementRepository announcementRepository; // --- ▼▼▼ [추가] 의존성 주입 ▼▼▼ ---
    private final FavoriteRepository favoriteRepository; // --- ▼▼▼ [추가] 의존성 주입 ▼▼▼ ---
    private final ReviewRepository reviewRepository; // --- ▼▼▼ [추가] 의존성 주입 ▼▼▼ ---
    private final UserActivityLogRepository userActivityLogRepository;



    public DashboardResponseDto getDashboardData(String range) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentPeriodStart;
        LocalDateTime prevPeriodStart;
        LocalDateTime prevPeriodEnd;

        switch (range.toLowerCase()) {
            // --- ▼▼▼ [추가] Daily 케이스 추가 ▼▼▼ ---
            case "daily":
                currentPeriodStart = now.toLocalDate().atStartOfDay();
                prevPeriodStart = currentPeriodStart.minusDays(1);
                prevPeriodEnd = currentPeriodStart.minusNanos(1);
                break;
            // --- ▲▲▲ [추가] Daily 케이스 추가 ▲▲▲ ---
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

    // --- ▼▼▼ [추가] 장소 상세 정보 조회 서비스 메서드 ▼▼▼ ---
    public AdminPlaceDetailDto getPlaceDetails(Long placeId) {
        // --- ▼▼▼ [수정] findById를 새로 만든 JOIN FETCH 쿼리 메서드로 변경 ▼▼▼ ---
        Place place = placeRepository.findByIdWithEditRequests(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));
        return new AdminPlaceDetailDto(place);
    }

    public List<AdminPlaceDto> getAllPlaces(String searchTerm) {
        List<Place> places;
        if (StringUtils.hasText(searchTerm)) {
            places = placeRepository.findByRoadAddressContaining(searchTerm);
        } else {
            places = placeRepository.findAll();
        }

        // --- ▼▼▼ [수정] 즐겨찾기 수와 수정 요청 수를 각각 조회하여 Map으로 변환 ▼▼▼ ---
        Map<Long, Long> favoriteCounts = favoriteRepository.countFavoritesByPlace().stream()
                .collect(Collectors.toMap(result -> (Long) result[0], result -> (Long) result[1]));

        // --- ▼▼▼ [추가] 리뷰 수 조회 로직 ▼▼▼ ---
        Map<Long, Long> reviewCounts = reviewRepository.countReviewsByPlace().stream()
                .collect(Collectors.toMap(result -> (Long) result[0], result -> (Long) result[1]));

        Map<Long, Long> pendingCounts = editRequestRepository.findAllByStatus(RequestStatus.PENDING)
                .stream()
                .collect(Collectors.groupingBy(request -> request.getPlace().getId(), Collectors.counting()));


        return places.stream()
                .map(place -> new AdminPlaceDto(
                        place,
                        favoriteCounts.getOrDefault(place.getId(), 0L).intValue(),
                        reviewCounts.getOrDefault(place.getId(), 0L).intValue(), // --- [추가] DTO에 리뷰 수 전달
                        pendingCounts.getOrDefault(place.getId(), 0L).intValue()
                ))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"allPlaces", "searchResults", "places"}, allEntries = true)
    @Transactional
    public void updatePlaceDescription(Long placeId, PlaceUpdateRequestDto requestDto) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        place.updateDescription(requestDto.getDescription());

        List<EditRequest> pendingRequests = editRequestRepository.findAllByPlaceAndStatus(place, RequestStatus.PENDING);
        pendingRequests.forEach(request -> request.updateStatus(RequestStatus.REVIEWED));
    }

    @CacheEvict(value = {"allPlaces", "searchResults", "places"}, allEntries = true)
    @Transactional
    public void deletePlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        place.getImageUrls().forEach(s3Uploader::delete);

        placeRepository.delete(place);
    }

    public List<AdminImageDto> getPlaceImages(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));
        return place.getImageInfos().stream()
                .map(AdminImageDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePlaceImage(Long placeId, String imageUrl) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        ImageInfo imageToRemove = place.getImageInfos().stream()
                .filter(img -> img.getImageUrl().equals(imageUrl))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 이미지를 찾을 수 없습니다."));

        place.getImageInfos().remove(imageToRemove);

        s3Uploader.delete(imageUrl);
    }

    @Transactional
    public void setRepresentativeImage(Long placeId, Long imageInfoId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        for (ImageInfo imageInfo : place.getImageInfos()) {
            imageInfo.setRepresentative(false);
        }

        ImageInfo newRepresentativeImage = place.getImageInfos().stream()
                .filter(img -> img.getId().equals(imageInfoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 이미지를 찾을 수 없습니다. id=" + imageInfoId));

        newRepresentativeImage.setRepresentative(true);
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
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = todayStart.with(DayOfWeek.MONDAY);
        LocalDateTime monthStart = todayStart.withDayOfMonth(1);
        LocalDateTime yearStart = todayStart.withDayOfYear(1);

        long placesDaily = placeRepository.countByCreatedAtAfter(todayStart);
        long placesWeekly = placeRepository.countByCreatedAtAfter(weekStart);
        long placesMonthly = placeRepository.countByCreatedAtAfter(monthStart);
        long placesYearly = placeRepository.countByCreatedAtAfter(yearStart);

        long usersDaily = userRepository.countByCreatedAtAfter(todayStart);
        long usersWeekly = userRepository.countByCreatedAtAfter(weekStart);
        long usersMonthly = userRepository.countByCreatedAtAfter(monthStart);
        long usersYearly = userRepository.countByCreatedAtAfter(yearStart);

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
        Map<Place, List<Report>> groupedByPlace = reportRepository.findAll().stream()
                .collect(Collectors.groupingBy(Report::getPlace));

        return groupedByPlace.entrySet().stream()
                .map(entry -> {
                    Place place = entry.getKey();
                    List<Report> reports = entry.getValue();

                    Map<String, Long> typeCounts = reports.stream()
                            .collect(Collectors.groupingBy(
                                    report -> report.getType().getDescription(),
                                    Collectors.counting()
                            ));

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

    // --- ▼▼▼ [추가] 공지 관리 기능 ▼▼▼ ---
    public List<AdminAnnouncementDto> getAllAnnouncements() {
        return announcementRepository.findAll().stream()
                .map(AdminAnnouncementDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createAnnouncement(AnnouncementSaveRequestDto requestDto) {
        Announcement announcement = requestDto.toEntity();
        return announcementRepository.save(announcement).getId();
    }

    @Transactional
    public void updateAnnouncement(Long announcementId, AnnouncementUpdateRequestDto requestDto) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지가 없습니다. id=" + announcementId));

        announcement.update(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.isActive(),
                requestDto.getStartDate(),
                requestDto.getEndDate()
        );
    }

    // --- ▼▼▼ [추가] 공지 활성 상태 토글 메서드 ▼▼▼ ---
    @Transactional
    public void toggleAnnouncementActive(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지가 없습니다. id=" + announcementId));
        announcement.toggleActive();
    }
    // --- ▲▲▲ [추가] 공지 활성 상태 토글 메서드 ▲▲▲ ---


    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        announcementRepository.deleteById(announcementId);
    }
    // --- ▲▲▲ [추가] 공지 관리 기능 ▲▲▲ ---

    // --- ▼▼▼ [추가] 대시보드 차트 데이터 생성 메서드 ▼▼▼ ---
    public DashboardChartResponseDto getDashboardChartData(String range) {
        List<ChartDataPointDto> chartData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        switch (range.toLowerCase()) {
            case "daily":
                chartData = getDailyChartData(today);
                break;
            case "weekly":
                chartData = getWeeklyChartData(today);
                break;
            case "monthly":
                chartData = getMonthlyChartData(today);
                break;
            case "yearly":
                chartData = getYearlyChartData(today);
                break;
        }
        return new DashboardChartResponseDto(chartData);
    }

    private List<ChartDataPointDto> generateDailyBreakdownChartData(LocalDate startDate, int days) {
        LocalDateTime startOfPeriod = startDate.atStartOfDay();
        LocalDateTime endOfPeriod = startOfPeriod.plusDays(days);

        long initialTotalPlaces = placeRepository.countByCreatedAtBefore(startOfPeriod);
        long initialTotalUsers = userRepository.countByCreatedAtBefore(startOfPeriod);

        List<Place> newPlacesInPeriod = placeRepository.findAllByCreatedAtBetween(startOfPeriod, endOfPeriod);
        List<User> newUsersInPeriod = userRepository.findAllByCreatedAtBetween(startOfPeriod, endOfPeriod);

        return IntStream.range(0, days)
                .mapToObj(i -> {
                    LocalDate currentDay = startDate.plusDays(i);
                    LocalDateTime startOfDay = currentDay.atStartOfDay();
                    LocalDateTime endOfDay = startOfDay.plusDays(1);

                    long dailyNewPlaces = newPlacesInPeriod.stream().filter(p -> !p.getCreatedAt().isBefore(startOfDay) && p.getCreatedAt().isBefore(endOfDay)).count();
                    long dailyNewUsers = newUsersInPeriod.stream().filter(u -> !u.getCreatedAt().isBefore(startOfDay) && u.getCreatedAt().isBefore(endOfDay)).count();

                    long cumulativePlaces = initialTotalPlaces + newPlacesInPeriod.stream().filter(p -> p.getCreatedAt().isBefore(endOfDay)).count();
                    long cumulativeUsers = initialTotalUsers + newUsersInPeriod.stream().filter(u -> u.getCreatedAt().isBefore(endOfDay)).count();

                    String label = String.format("%s(%s)",
                            currentDay.format(DateTimeFormatter.ofPattern("MM-dd")),
                            currentDay.format(DateTimeFormatter.ofPattern("E"))
                    );

                    return new ChartDataPointDto(
                            label,
                            dailyNewPlaces,
                            dailyNewUsers,
                            cumulativePlaces,
                            cumulativeUsers
                    );
                })
                .collect(Collectors.toList());
    }

    // --- ▼▼▼ [수정] Daily 로직: 오늘 포함 과거 7일 ▼▼▼ ---
    private List<ChartDataPointDto> getDailyChartData(LocalDate today) {
        LocalDate startDate = today.minusDays(6);
        return generateDailyBreakdownChartData(startDate, 7);
    }

    // --- ▼▼▼ [수정] Weekly 로직: 이번 주 포함 과거 7주 ▼▼▼ ---
    private List<ChartDataPointDto> getWeeklyChartData(LocalDate today) {
        List<ChartDataPointDto> data = new ArrayList<>();
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);

        for (int i = 6; i >= 0; i--) {
            LocalDate dateInWeek = today.minusWeeks(i);
            LocalDate startOfWeek = dateInWeek.with(DayOfWeek.MONDAY);
            LocalDateTime start = startOfWeek.atStartOfDay();
            LocalDateTime end = startOfWeek.plusWeeks(1).atStartOfDay();

            long weeklyNewPlaces = placeRepository.countByCreatedAtBetween(start, end);
            long weeklyNewUsers = userRepository.countByCreatedAtBetween(start, end);
            long totalPlaces = placeRepository.countByCreatedAtBefore(end);
            long totalUsers = userRepository.countByCreatedAtBefore(end);

            String label = String.format("%d월 %d주차", startOfWeek.getMonthValue(), startOfWeek.get(weekFields.weekOfMonth()));
            data.add(new ChartDataPointDto(label, weeklyNewPlaces, weeklyNewUsers, totalPlaces, totalUsers));
        }
        return data;
    }

    private List<ChartDataPointDto> getMonthlyChartData(LocalDate today) {
        List<ChartDataPointDto> data = new ArrayList<>();
        LocalDate firstDayOfYear = today.withDayOfYear(1);

        for (int i = 0; i < 12; i++) {
            LocalDate dateInMonth = firstDayOfYear.plusMonths(i);
            if(dateInMonth.isAfter(today)) break;

            LocalDateTime start = dateInMonth.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = dateInMonth.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).atStartOfDay();

            long monthlyNewPlaces = placeRepository.countByCreatedAtBetween(start, end);
            long monthlyNewUsers = userRepository.countByCreatedAtBetween(start, end);
            long totalPlaces = placeRepository.countByCreatedAtBefore(end);
            long totalUsers = userRepository.countByCreatedAtBefore(end);

            data.add(new ChartDataPointDto(dateInMonth.format(DateTimeFormatter.ofPattern("MMM")), monthlyNewPlaces, monthlyNewUsers, totalPlaces, totalUsers));
        }
        return data;
    }

    private List<ChartDataPointDto> getYearlyChartData(LocalDate today) {
        List<ChartDataPointDto> data = new ArrayList<>();
        int currentYear = today.getYear();

        for (int i = 4; i >= 0; i--) {
            int year = currentYear - i;
            LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
            LocalDateTime end = start.plusYears(1);

            long yearlyNewPlaces = placeRepository.countByCreatedAtBetween(start, end);
            long yearlyNewUsers = userRepository.countByCreatedAtBetween(start, end);
            long totalPlaces = placeRepository.countByCreatedAtBefore(end);
            long totalUsers = userRepository.countByCreatedAtBefore(end);

            data.add(new ChartDataPointDto(String.valueOf(year), yearlyNewPlaces, yearlyNewUsers, totalPlaces, totalUsers));
        }
        return data;
    }

    // --- ▼▼▼ [추가] 관리자 리뷰 삭제 메서드 ▼▼▼ ---
    @Transactional
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        Place place = review.getPlace();
        reviewRepository.delete(review);

        // --- ▼▼▼ [수정] 새로운 Repository 메서드를 사용하도록 변경 ▼▼▼ ---
        // 1. 평균 평점 조회
        Double averageRating = reviewRepository.findAverageRatingByPlace(place);

        // 2. 리뷰 수 조회
        Long reviewCount = reviewRepository.countByPlace(place);

        // 3. Place 엔티티 업데이트 (null일 경우 기본값 0으로 처리)
        place.updateReviewStats(
                averageRating != null ? averageRating : 0.0,
                reviewCount != null ? reviewCount.intValue() : 0
        );
        // --- ▲▲▲ [수정] 새로운 Repository 메서드를 사용하도록 변경 ▲▲▲ ---
    }

    public List<AdminActivityLogDto> getActivityLogs() {
        List<UserActivityLog> logs = userActivityLogRepository.findTop100ByOrderByIdDesc();

        List<Long> userIds = logs.stream()
                .map(UserActivityLog::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return logs.stream()
                .map(log -> {
                    String userType;
                    String identifier;

                    if (log.getUserId() != null) {
                        userType = "로그인";
                        User user = userMap.get(log.getUserId());
                        identifier = (user != null && user.getEmail() != null) ? user.getEmail() : "ID: " + log.getUserId() + " (사용자 정보 없음)";
                    } else {
                        userType = "비회원";
                        identifier = log.getSessionId();
                    }

                    return AdminActivityLogDto.builder()
                            .id(log.getId())
                            .activityTime(log.getCreatedAt().format(formatter))
                            .latitude(log.getLatitude())
                            .longitude(log.getLongitude())
                            .userType(userType)
                            .identifier(identifier)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<HeatmapDto> getHeatmapData() {
        List<UserActivityLog> logs = userActivityLogRepository.findAll();
        return logs.stream()
                .map(log -> new HeatmapDto(log.getLatitude(), log.getLongitude()))
                .collect(Collectors.toList());
    }
}