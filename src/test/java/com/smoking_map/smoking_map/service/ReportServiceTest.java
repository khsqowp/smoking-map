package com.smoking_map.smoking_map.service;

import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.report.Report;
import com.smoking_map.smoking_map.domain.report.ReportRepository;
import com.smoking_map.smoking_map.domain.report.ReportType;
import com.smoking_map.smoking_map.domain.user.Role;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.ReportRequestDto;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService 단위 테스트")
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpSession httpSession;

    private User user;
    private User admin;
    private Place place;
    private ReportRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("일반유저")
                .role(Role.USER)
                .build();

        admin = User.builder()
                .id(2L)
                .email("admin@example.com")
                .name("관리자")
                .role(Role.ADMIN)
                .build();

        place = Place.builder()
                .id(1L)
                .build();

        requestDto = new ReportRequestDto(ReportType.INACCURATE, "정보가 정확하지 않아요.");
    }

    @Nested
    @DisplayName("submitReport (장소 제보) 테스트")
    class Describe_submitReport {

        @Test
        @DisplayName("로그인한 일반사용자(USER)가 처음으로 제보하면, 성공적으로 등록된다")
        void 성공_일반사용자_첫_제보() {
            // 준비 (Arrange)
            // 왜? 로그인한 사용자가 제보하는 핵심 기능이 정상 동작하는지, 사용자 정보가 Report에 올바르게 연결되는지 검증하기 위함.
            SessionUser sessionUser = new SessionUser(user);
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(reportRepository.existsByUserIdAndPlaceId(user.getId(), place.getId())).willReturn(false);

            // 실행 (Act)
            reportService.submitReport(place.getId(), requestDto);

            // 검증 (Assert)
            ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
            verify(reportRepository, times(1)).save(reportCaptor.capture());
            Report savedReport = reportCaptor.getValue();

            assertThat(savedReport.getUser()).isEqualTo(user);
            assertThat(savedReport.getPlace()).isEqualTo(place);
            assertThat(savedReport.getType()).isEqualTo(requestDto.getType());
            assertThat(savedReport.getContent()).isEqualTo(requestDto.getContent());
        }

        @Test
        @DisplayName("익명 사용자가 제보하면, 사용자 정보 없이 성공적으로 등록된다")
        void 성공_익명사용자_제보() {
            // 준비 (Arrange)
            // 왜? 비로그인 상태에서도 제보가 가능한지, 이 경우 Report의 user 필드가 null로 저장되는지 검증하기 위함.
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(httpSession.getAttribute("user")).willReturn(null);

            // 실행 (Act)
            reportService.submitReport(place.getId(), requestDto);

            // 검증 (Assert)
            ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
            verify(reportRepository, times(1)).save(reportCaptor.capture());
            Report savedReport = reportCaptor.getValue();

            assertThat(savedReport.getUser()).isNull(); // 익명 사용자의 제보이므로 user는 null이어야 함
            assertThat(savedReport.getPlace()).isEqualTo(place);
        }

        @Test
        @DisplayName("로그인한 일반사용자(USER)가 동일한 장소에 중복으로 제보하면, IllegalArgumentException이 발생한다")
        void 실패_일반사용자_중복_제보() {
            // 준비 (Arrange)
            // 왜? 일반 사용자의 무분별한 중복 제보를 방지하는 핵심 비즈니스 로직이 올바르게 동작하는지 검증해야 함.
            SessionUser sessionUser = new SessionUser(user);
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(reportRepository.existsByUserIdAndPlaceId(user.getId(), place.getId())).willReturn(true); // 이미 제보한 상태

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> reportService.submitReport(place.getId(), requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 이 장소에 대해 신고한 내역이 있습니다.");
        }

        @Test
        @DisplayName("관리자(ADMIN)는 동일한 장소에 중복으로 제보할 수 있다")
        void 성공_관리자_중복_제보() {
            // 준비 (Arrange)
            // 왜? 관리자는 일반 사용자와 다른 정책을 가질 수 있음. 중복 제보 방지 로직이 관리자에게는 적용되지 않는다는 특정 규칙을 검증하기 위함.
            SessionUser sessionAdmin = new SessionUser(admin);
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(httpSession.getAttribute("user")).willReturn(sessionAdmin);
            given(userRepository.findByEmail(admin.getEmail())).willReturn(Optional.of(admin));
            // 관리자(ADMIN) 역할이므로 existsByUserIdAndPlaceId는 호출되지 않아야 함

            // 실행 (Act)
            reportService.submitReport(place.getId(), requestDto);

            // 검증 (Assert)
            verify(reportRepository, times(1)).save(any(Report.class));
            // 관리자에게는 중복 체크 로직이 실행되지 않았는지 확인
            verify(reportRepository, never()).existsByUserIdAndPlaceId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("제보하려는 장소를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_장소_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 데이터에 대한 요청을 올바르게 차단하는지 검증하기 위함.
            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> reportService.submitReport(999L, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 장소가 없습니다. id=999");
        }

        @Test
        @DisplayName("세션에 사용자는 있으나 DB에 없으면, 익명 제보로 처리된다")
        void 성공_세션만_있는_사용자() {
            // 준비 (Arrange)
            // 왜? 사용자가 탈퇴한 직후 만료되지 않은 세션으로 요청하는 등 예외적인 상황에서 시스템이 안전하게 동작하는지(익명 처리) 검증하기 위함.
            SessionUser sessionUser = new SessionUser(user);
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty()); // DB에 사용자가 없음

            // 실행 (Act)
            reportService.submitReport(place.getId(), requestDto);

            // 검증 (Assert)
            ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
            verify(reportRepository, times(1)).save(reportCaptor.capture());
            Report savedReport = reportCaptor.getValue();

            assertThat(savedReport.getUser()).isNull(); // 사용자 정보가 없으므로 null이어야 함
            assertThat(savedReport.getPlace()).isEqualTo(place);
        }
    }
}
