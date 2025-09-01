package com.smoking_map.smoking_map.service;

import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.domain.edit_request.EditRequest;
import com.smoking_map.smoking_map.domain.edit_request.EditRequestRepository;
import com.smoking_map.smoking_map.domain.edit_request.RequestStatus;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.EditRequestSaveDto;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EditRequestService 단위 테스트")
class EditRequestServiceTest {

    @InjectMocks
    private EditRequestService editRequestService;

    @Mock
    private EditRequestRepository editRequestRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private SessionUser sessionUser;
    private Place place;
    private EditRequestSaveDto requestDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트유저")
                .build();

        sessionUser = new SessionUser(user);

        place = Place.builder()
                .id(1L)
                .build();

        requestDto = new EditRequestSaveDto("여기는 이제 금연구역이에요.");
    }

    @Nested
    @DisplayName("saveEditRequest (수정 요청 저장) 테스트")
    class Describe_saveEditRequest {

        @Test
        @DisplayName("로그인한 사용자가 존재하는 장소에 수정을 요청하면, 성공적으로 등록된다")
        void 성공_수정_요청_등록() {
            // 준비 (Arrange)
            // 왜? 로그인한 사용자가 수정 요청을 보내는 핵심 기능이 정상 동작하는지, 요청 상태가 'PENDING'으로 올바르게 설정되는지 검증하기 위함.
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));

            // 실행 (Act)
            editRequestService.saveEditRequest(place.getId(), requestDto, sessionUser);

            // 검증 (Assert)
            ArgumentCaptor<EditRequest> captor = ArgumentCaptor.forClass(EditRequest.class);
            verify(editRequestRepository, times(1)).save(captor.capture());
            EditRequest savedRequest = captor.getValue();

            assertThat(savedRequest.getUser()).isEqualTo(user);
            assertThat(savedRequest.getPlace()).isEqualTo(place);
            assertThat(savedRequest.getContent()).isEqualTo(requestDto.getContent());
            // 상태가 PENDING으로 저장되었는지 확인
            assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
        }

        @Test
        @DisplayName("요청 DTO의 content가 null이거나 비어있어도, 요청은 등록된다")
        void 성공_내용이_없는_요청() {
            // 준비 (Arrange)
            // 왜? 수정 요청 내용이 없는 경우도 비즈니스적으로 유효할 수 있으므로(예: 위치만 잘못되었다고 알리는 경우), content가 없어도 막히지 않는지 확인하기 위함.
            EditRequestSaveDto emptyContentDto = new EditRequestSaveDto("");
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));

            // 실행 (Act)
            editRequestService.saveEditRequest(place.getId(), emptyContentDto, sessionUser);

            // 검증 (Assert)
            ArgumentCaptor<EditRequest> captor = ArgumentCaptor.forClass(EditRequest.class);
            verify(editRequestRepository, times(1)).save(captor.capture());
            assertThat(captor.getValue().getContent()).isEqualTo("");
        }

        @Test
        @DisplayName("세션 정보에 해당하는 사용자를 DB에서 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_사용자_없음() {
            // 준비 (Arrange)
            // 왜? 세션은 유효하지만 DB에 사용자가 없는 비정상적인 경우(예: 탈퇴 직후)를 올바르게 차단하는지 검증하기 위함.
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> editRequestService.saveEditRequest(place.getId(), requestDto, sessionUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 사용자가 없습니다. email=");
        }

        @Test
        @DisplayName("수정 요청하려는 장소를 DB에서 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_장소_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 장소에 대한 수정 요청을 차단하는지 검증하기 위함.
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> editRequestService.saveEditRequest(999L, requestDto, sessionUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 장소가 없습니다. id=");
        }

        @Test
        @DisplayName("세션 정보가 null이면, NullPointerException이 발생한다")
        void 실패_세션_없음() {
            // 준비 (Arrange)
            // 왜? 이 서비스는 로그인이 반드시 필요한 기능이므로, 세션이 없는 비정상적인 호출을 차단하는지 확인해야 함.
            // 컨트롤러 레벨에서 차단되겠지만, 서비스 자체의 견고성을 위해 테스트함.

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> editRequestService.saveEditRequest(place.getId(), requestDto, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
