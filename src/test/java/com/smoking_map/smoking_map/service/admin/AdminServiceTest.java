package com.smoking_map.smoking_map.service.admin;

import com.smoking_map.smoking_map.domain.user.Role;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.admin.AdminUserDto;
import com.smoking_map.smoking_map.web.dto.admin.UserContributionDto;
import com.smoking_map.smoking_map.web.dto.admin.UserRoleUpdateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService - 사용자 관리 단위 테스트")
class AdminServiceUserManagementTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("유저1")
                .email("user1@example.com")
                .role(Role.USER)
                .build();

        user2 = User.builder()
                .id(2L)
                .name("유저2")
                .email("user2@example.com")
                .role(Role.ADMIN)
                .build();
    }

    @Nested
    @DisplayName("getAllUsers (모든 사용자 조회) 테스트")
    class Describe_getAllUsers {

        @Test
        @DisplayName("사용자가 존재할 경우, 모든 사용자 목록을 AdminUserDto 리스트로 반환한다")
        void 성공_사용자_목록_반환() {
            // 준비 (Arrange)
            // 왜? DB의 모든 User 엔티티를 DTO로 정확하게 변환하여 반환하는지 기본 흐름을 검증하기 위함.
            given(userRepository.findAll()).willReturn(List.of(user1, user2));

            // 실행 (Act)
            List<AdminUserDto> result = adminService.getAllUsers();

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getEmail()).isEqualTo(user1.getEmail());
            assertThat(result.get(1).getEmail()).isEqualTo(user2.getEmail());
        }

        @Test
        @DisplayName("사용자가 한 명도 없을 경우, 빈 리스트를 반환한다")
        void 성공_사용자_없음() {
            // 준비 (Arrange)
            // 왜? 데이터가 없는 경계 조건에서 시스템이 오류 없이 정상적으로 빈 결과를 반환하는지 확인하기 위함.
            given(userRepository.findAll()).willReturn(List.of());

            // 실행 (Act)
            List<AdminUserDto> result = adminService.getAllUsers();

            // 검증 (Assert)
            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserContributions (사용자 기여도 조회) 테스트")
    class Describe_getUserContributions {

        @Test
        @DisplayName("사용자별 장소 등록 수를 조회하고, 등록 수가 많은 순으로 정렬하여 반환한다")
        void 성공_기여도_조회_및_정렬() {
            // 준비 (Arrange)
            // 왜? 각 사용자의 기여도(장소 등록 수)를 정확히 집계하고, 기여도가 높은 순으로 정렬하는 로직을 검증하기 위함.
            Object[] contribution1 = {user1, 5L}; // user1은 5개 등록
            Object[] contribution2 = {user2, 10L}; // user2는 10개 등록
            given(userRepository.countPlacesByUser()).willReturn(List.of(contribution1, contribution2));

            // 실행 (Act)
            List<UserContributionDto> result = adminService.getUserContributions();

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            // 등록 수가 많은 user2가 첫 번째로 오는지 확인
            assertThat(result.get(0).getUserId()).isEqualTo(user2.getId());
            assertThat(result.get(0).getPlaceCount()).isEqualTo(10L);
            // 등록 수가 적은 user1이 두 번째로 오는지 확인
            assertThat(result.get(1).getUserId()).isEqualTo(user1.getId());
            assertThat(result.get(1).getPlaceCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("기여도 데이터가 없을 경우, 빈 리스트를 반환한다")
        void 성공_기여도_없음() {
            // 준비 (Arrange)
            // 왜? 데이터가 없는 경계 조건에서 정상적으로 빈 결과를 반환하는지 확인하기 위함.
            given(userRepository.countPlacesByUser()).willReturn(List.of());

            // 실행 (Act)
            List<UserContributionDto> result = adminService.getUserContributions();

            // 검증 (Assert)
            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("updateUserRole (사용자 역할 변경) 테스트")
    class Describe_updateUserRole {

        @Test
        @DisplayName("존재하는 사용자의 역할을 변경하면, 성공적으로 적용된다")
        void 성공_역할_변경() {
            // 준비 (Arrange)
            // 왜? 관리자가 사용자의 권한을 변경하는 핵심 보안 기능이 정상 동작하는지 검증하기 위함.
            UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto("ADMIN");
            given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));

            // 실행 (Act)
            adminService.updateUserRole(user1.getId(), requestDto);

            // 검증 (Assert)
            // user1의 Role이 ADMIN으로 변경되었는지 확인
            assertThat(user1.getRole()).isEqualTo(Role.ADMIN);
            // userRepository.save가 호출되었는지 간접적으로 확인 (JPA의 더티 체킹)
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 역할 변경을 시도하면, IllegalArgumentException이 발생한다")
        void 실패_사용자_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 대상에 대한 수정을 시도할 때, 적절한 예외를 발생시켜 잘못된 연산을 방지하는지 확인해야 함.
            UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto("ADMIN");
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> adminService.updateUserRole(999L, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 사용자가 없습니다.");
        }

        @Test
        @DisplayName("유효하지 않은 역할 이름으로 변경을 시도하면, IllegalArgumentException이 발생한다")
        void 실패_유효하지_않은_역할() {
            // 준비 (Arrange)
            // 왜? 정의되지 않은 값(Role Enum에 없는 값)으로 시스템 상태를 변경하려는 시도를 차단하는지, 입력값 검증을 확인하기 위함.
            UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto("INVALID_ROLE");
            given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));

            // 실행 및 검증 (Act & Assert)
            // Role.valueOf()에서 발생하는 예외를 확인
            assertThatThrownBy(() -> adminService.updateUserRole(user1.getId(), requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No enum constant com.smoking_map.smoking_map.domain.user.Role.INVALID_ROLE");
        }
    }
}
