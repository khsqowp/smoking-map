package com.smoking_map.smoking_map.service.admin;

import com.smoking_map.smoking_map.domain.activity_log.UserActivityLog;
import com.smoking_map.smoking_map.domain.activity_log.UserActivityLogRepository;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.report.Report;
import com.smoking_map.smoking_map.domain.report.ReportRepository;
import com.smoking_map.smoking_map.domain.report.ReportType;
import com.smoking_map.smoking_map.domain.review.Review;
import com.smoking_map.smoking_map.domain.review.ReviewRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.admin.DashboardResponseDto;
import com.smoking_map.smoking_map.web.dto.admin.GroupedAdminReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService - 데이터 및 관리 단위 테스트")
class AdminServiceDataAndModerationTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private UserActivityLogRepository userActivityLogRepository;

    @Mock
    private UserRepository userRepository;

    private Place place1;
    private User user1;

    @BeforeEach
    void setUp() {
        place1 = Place.builder().id(1L).roadAddress("테스트 주소").build();
        user1 = User.builder().id(1L).email("test@example.com").build();
    }

    @Nested
    @DisplayName("getGroupedReports (그룹화된 제보 조회) 테스트")
    class Describe_getGroupedReports {

        @Test
        @DisplayName("제보들을 장소별로 그룹화하고, 타입별 개수와 기타 내용을 집계하여 반환한다")
        void 성공_제보_그룹화() {
            // 준비 (Arrange)
            // 왜? 여러 제보 데이터를 특정 기준(장소)으로 그룹화하고, 그룹 내에서 2차 집계(타입별 개수)를 수행하는 복잡한 데이터 가공 로직을 검증하기 위함.
            Report report1 = Report.builder().place(place1).type(ReportType.INACCURATE).build();
            Report report2 = Report.builder().place(place1).type(ReportType.INACCURATE).build();
            Report report3 = Report.builder().place(place1).type(ReportType.OTHER).content("기타 내용").build();
            given(reportRepository.findAll()).willReturn(List.of(report1, report2, report3));

            // 실행 (Act)
            List<GroupedAdminReportDto> result = adminService.getGroupedReports();

            // 검증 (Assert)
            assertThat(result).hasSize(1);
            GroupedAdminReportDto dto = result.get(0);
            assertThat(dto.getPlaceId()).isEqualTo(place1.getId());
            // INACCURATE 타입이 2개인지 확인
            assertThat(dto.getReportTypeCounts()).containsEntry(ReportType.INACCURATE.getDescription(), 2L);
            // OTHER 타입의 내용이 올바르게 포함되었는지 확인
            assertThat(dto.getOtherContents()).containsExactly("기타 내용");
        }
    }

    @Nested
    @DisplayName("deleteReviewByAdmin (관리자 리뷰 삭제) 테스트")
    class Describe_deleteReviewByAdmin {

        @Test
        @DisplayName("리뷰를 삭제하고, 해당 장소의 통계를 업데이트한다")
        void 성공_리뷰_삭제_및_통계_업데이트() {
            // 준비 (Arrange)
            // 왜? 관리자가 리뷰를 삭제하는 기능과, 삭제 후 연관된 장소의 상태(평점, 리뷰 수)를 올바르게 업데이트하는지 검증하기 위함.
            Review review = Review.builder().id(1L).place(place1).rating(5).build();
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            // 삭제 후 리뷰가 0개가 되는 상황을 가정
            given(reviewRepository.findAverageRatingByPlace(place1)).willReturn(null);
            given(reviewRepository.countByPlace(place1)).willReturn(0L);

            // 실행 (Act)
            adminService.deleteReviewByAdmin(1L);

            // 검증 (Assert)
            verify(reviewRepository).delete(review);
            // 통계가 0으로 초기화되었는지 확인
            assertThat(place1.getAverageRating()).isEqualTo(0.0);
            assertThat(place1.getReviewCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("삭제할 리뷰가 없으면, IllegalArgumentException이 발생한다")
        void 실패_리뷰_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 데이터를 삭제하려는 시도를 차단하는지 검증하기 위함.
            given(reviewRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> adminService.deleteReviewByAdmin(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Review not found");
        }
    }

    @Nested
    @DisplayName("getDashboardData (대시보드 데이터 조회) 테스트")
    class Describe_getDashboardData {

        @Test
        @DisplayName("기간(range)을 'daily'로 지정했을 때, 일일 데이터를 기준으로 통계를 계산하여 반환한다")
        void 성공_일일_통계() {
            // 준비 (Arrange)
            // 왜? 기간별로 분기되는 로직 중 'daily' 케이스가 정확한 날짜 범위를 설정하고, 이를 기반으로 올바른 통계(증감율 등)를 계산하는지 검증하기 위함.
            given(placeRepository.count()).willReturn(100L);
            given(userRepository.count()).willReturn(50L);
            // 현재 기간(오늘) 데이터
            given(placeRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).willReturn(10L);
            given(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).willReturn(5L);
            // 이전 기간(어제) 데이터
            given(placeRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).willReturn(8L);
            given(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).willReturn(4L);

            // 실행 (Act)
            DashboardResponseDto result = adminService.getDashboardData("daily");

            // 검증 (Assert)
            assertThat(result.getTotalPlaces()).isEqualTo(100L);
            assertThat(result.getTotalUsers()).isEqualTo(50L);
            assertThat(result.getPeriodPlacesCount()).isEqualTo(10L);
            // 증감율 계산: ((10 - 8) / 8) * 100 = 25.0
            assertThat(result.getPlacesGrowthRate()).isEqualTo(25.0);
            // 증감율 계산: ((5 - 4) / 4) * 100 = 25.0
            assertThat(result.getUsersGrowthRate()).isEqualTo(25.0);
        }

        @Test
        @DisplayName("이전 기간 데이터가 0일 때, 증감율이 0 또는 100으로 올바르게 계산된다 (0으로 나누기 방지)")
        void 성공_이전_데이터가_0일때_증감율_계산() {
            // 준비 (Arrange)
            // 왜? 분모가 0이 되는 경계값(엣지 케이스)에서 0으로 나누기 오류(DivideByZero)가 발생하지 않고, 비즈니스 규칙(현재 값 > 0 이면 100%, 아니면 0%)에 따라 증감율이 처리되는지 검증하기 위함.
            given(placeRepository.count()).willReturn(10L);
            given(userRepository.count()).willReturn(5L);
            given(placeRepository.countByCreatedAtBetween(any(), any())).willReturn(10L, 0L); // 현재 10, 과거 0
            given(userRepository.countByCreatedAtBetween(any(), any())).willReturn(0L, 0L); // 현재 0, 과거 0

            // 실행 (Act)
            DashboardResponseDto result = adminService.getDashboardData("weekly");

            // 검증 (Assert)
            // 과거 0, 현재 10 -> 100%
            assertThat(result.getPlacesGrowthRate()).isEqualTo(100.0);
            // 과거 0, 현재 0 -> 0%
            assertThat(result.getUsersGrowthRate()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("getActivityLogs (활동 로그 조회) 테스트")
    class Describe_getActivityLogs {

        @Test
        @DisplayName("로그인 및 비회원 활동 로그를 DTO로 변환하여 반환한다")
        void 성공_활동_로그_조회() {
            // 준비 (Arrange)
            // 왜? 두 종류의 로그(로그인/비회원)를 조회하고, 특히 로그인 로그의 경우 사용자 정보를 추가로 조합하여 완전한 DTO를 생성하는 복잡한 데이터 조합 로직을 검증하기 위함.
            UserActivityLog loggedInLog = UserActivityLog.builder().id(1L).userId(user1.getId()).createdAt(LocalDateTime.now()).build();
            UserActivityLog anonymousLog = UserActivityLog.builder().id(2L).sessionId("session-abc").createdAt(LocalDateTime.now()).build();

            given(userActivityLogRepository.findTop100ByOrderByIdDesc()).willReturn(List.of(loggedInLog, anonymousLog));
            given(userRepository.findAllById(List.of(user1.getId()))).willReturn(List.of(user1));

            // 실행 (Act)
            List<AdminActivityLogDto> result = adminService.getActivityLogs();

            // 검증 (Assert)
            assertThat(result).hasSize(2);

            AdminActivityLogDto loggedInDto = result.stream().filter(dto -> dto.getId().equals(1L)).findFirst().get();
            assertThat(loggedInDto.getUserType()).isEqualTo("로그인");
            assertThat(loggedInDto.getIdentifier()).isEqualTo(user1.getEmail());

            AdminActivityLogDto anonymousDto = result.stream().filter(dto -> dto.getId().equals(2L)).findFirst().get();
            assertThat(anonymousDto.getUserType()).isEqualTo("비회원");
            assertThat(anonymousDto.getIdentifier()).isEqualTo("session-abc");
        }
    }
}
