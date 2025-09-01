package com.smoking_map.smoking_map.service.admin;

import com.smoking_map.smoking_map.domain.announcement.Announcement;
import com.smoking_map.smoking_map.domain.announcement.AnnouncementRepository;
import com.smoking_map.smoking_map.web.dto.admin.AdminAnnouncementDto;
import com.smoking_map.smoking_map.web.dto.admin.AnnouncementSaveRequestDto;
import com.smoking_map.smoking_map.web.dto.admin.AnnouncementUpdateRequestDto;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService - 공지사항 관리 단위 테스트")
class AdminServiceAnnouncementManagementTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private AnnouncementRepository announcementRepository;

    private Announcement announcement1;
    private Announcement announcement2;

    @BeforeEach
    void setUp() {
        announcement1 = Announcement.builder()
                .id(1L)
                .title("공지1")
                .content("내용1")
                .active(true)
                .build();

        announcement2 = Announcement.builder()
                .id(2L)
                .title("공지2")
                .content("내용2")
                .active(false)
                .build();
    }

    @Nested
    @DisplayName("getAllAnnouncements (모든 공지사항 조회) 테스트")
    class Describe_getAllAnnouncements {
        @Test
        @DisplayName("모든 공지사항을 DTO 리스트로 변환하여 반환한다")
        void 성공_모든_공지_조회() {
            // 준비 (Arrange)
            // 왜? DB의 모든 Announcement 엔티티를 DTO로 정확하게 변환하는지 검증하기 위함.
            given(announcementRepository.findAll()).willReturn(List.of(announcement1, announcement2));

            // 실행 (Act)
            List<AdminAnnouncementDto> result = adminService.getAllAnnouncements();

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo(announcement1.getTitle());
        }
    }

    @Nested
    @DisplayName("createAnnouncement (공지사항 생성) 테스트")
    class Describe_createAnnouncement {
        @Test
        @DisplayName("DTO를 받아 새로운 공지사항을 생성하고 ID를 반환한다")
        void 성공_공지_생성() {
            // 준비 (Arrange)
            // 왜? DTO가 엔티티로 올바르게 변환되어 DB에 저장되는지 검증하기 위함.
            AnnouncementSaveRequestDto requestDto = new AnnouncementSaveRequestDto("새 공지", "새 내용", true, null, null);
            given(announcementRepository.save(any(Announcement.class))).willReturn(announcement1);

            // 실행 (Act)
            Long newId = adminService.createAnnouncement(requestDto);

            // 검증 (Assert)
            assertThat(newId).isEqualTo(1L);
            verify(announcementRepository, times(1)).save(any(Announcement.class));
        }
    }

    @Nested
    @DisplayName("updateAnnouncement (공지사항 수정) 테스트")
    class Describe_updateAnnouncement {
        @Test
        @DisplayName("존재하는 공지사항의 내용을 DTO에 따라 수정한다")
        void 성공_공지_수정() {
            // 준비 (Arrange)
            // 왜? 특정 공지사항의 속성들이 DTO의 값으로 정확하게 변경되는지 검증하기 위함.
            LocalDateTime newEndDate = LocalDateTime.now().plusDays(10);
            AnnouncementUpdateRequestDto requestDto = new AnnouncementUpdateRequestDto("수정된 제목", "수정된 내용", false, null, newEndDate);
            given(announcementRepository.findById(1L)).willReturn(Optional.of(announcement1));

            // 실행 (Act)
            adminService.updateAnnouncement(1L, requestDto);

            // 검증 (Assert)
            assertThat(announcement1.getTitle()).isEqualTo("수정된 제목");
            assertThat(announcement1.getContent()).isEqualTo("수정된 내용");
            assertThat(announcement1.isActive()).isFalse();
            assertThat(announcement1.getEndDate()).isEqualTo(newEndDate);
        }

        @Test
        @DisplayName("존재하지 않는 공지사항을 수정하려 하면, IllegalArgumentException이 발생한다")
        void 실패_공지_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 데이터에 대한 수정 시도를 차단하는지 검증하기 위함.
            AnnouncementUpdateRequestDto requestDto = new AnnouncementUpdateRequestDto("수정된 제목", "수정된 내용", false, null, null);
            given(announcementRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> adminService.updateAnnouncement(999L, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 공지가 없습니다.");
        }
    }

    @Nested
    @DisplayName("toggleAnnouncementActive (공지사항 활성 상태 변경) 테스트")
    class Describe_toggleAnnouncementActive {
        @Test
        @DisplayName("활성 상태의 공지를 비활성으로 변경한다")
        void 성공_활성에서_비활성으로() {
            // 준비 (Arrange)
            // 왜? 상태 변경(toggle) 로직이 정상적으로 동작하는지 검증하기 위함 (true -> false).
            announcement1.setActive(true);
            given(announcementRepository.findById(1L)).willReturn(Optional.of(announcement1));

            // 실행 (Act)
            adminService.toggleAnnouncementActive(1L);

            // 검증 (Assert)
            assertThat(announcement1.isActive()).isFalse();
        }

        @Test
        @DisplayName("비활성 상태의 공지를 활성으로 변경한다")
        void 성공_비활성에서_활성으로() {
            // 준비 (Arrange)
            // 왜? 상태 변경(toggle) 로직이 정상적으로 동작하는지 검증하기 위함 (false -> true).
            announcement2.setActive(false);
            given(announcementRepository.findById(2L)).willReturn(Optional.of(announcement2));

            // 실행 (Act)
            adminService.toggleAnnouncementActive(2L);

            // 검증 (Assert)
            assertThat(announcement2.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteAnnouncement (공지사항 삭제) 테스트")
    class Describe_deleteAnnouncement {
        @Test
        @DisplayName("ID를 받아 해당 공지사항을 삭제한다")
        void 성공_공지_삭제() {
            // 준비 (Arrange)
            // 왜? 특정 데이터를 삭제하는 기능이 Repository와 올바르게 상호작용하는지 검증하기 위함.
            // deleteById는 반환값이 없으므로, Mockito.verify를 통해 호출 여부만 검증한다.

            // 실행 (Act)
            adminService.deleteAnnouncement(1L);

            // 검증 (Assert)
            verify(announcementRepository, times(1)).deleteById(1L);
        }
    }
}
