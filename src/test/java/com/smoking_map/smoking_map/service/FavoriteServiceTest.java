package com.smoking_map.smoking_map.service;

import com.smoking_map.smoking_map.domain.favorite.Favorite;
import com.smoking_map.smoking_map.domain.favorite.FavoriteRepository;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.PlaceResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteService 단위 테스트")
class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlaceRepository placeRepository;

    private User user;
    private Place place;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트유저")
                .build();

        place = Place.builder()
                .id(1L)
                .roadAddress("서울 테스트 주소")
                .build();
    }

    @Nested
    @DisplayName("addFavorite (즐겨찾기 추가) 테스트")
    class Describe_addFavorite {

        @Test
        @DisplayName("정상적인 요청 시, 즐겨찾기를 성공적으로 추가한다")
        void 성공_즐겨찾기_추가() {
            // 준비 (Arrange)
            // 왜? 사용자와 장소가 유효하고, 아직 즐겨찾기하지 않은 상태에서 즐겨찾기를 추가하는 핵심 기능이 정상 동작하는지 검증하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(favoriteRepository.existsByUserAndPlace(user, place)).willReturn(false);

            // 실행 (Act)
            favoriteService.addFavorite(user.getEmail(), place.getId());

            // 검증 (Assert)
            verify(favoriteRepository, times(1)).save(any(Favorite.class));
        }

        @Test
        @DisplayName("이미 즐겨찾기한 장소를 다시 추가하려 하면, IllegalStateException이 발생한다")
        void 실패_이미_즐겨찾기된_장소() {
            // 준비 (Arrange)
            // 왜? 중복 데이터 생성을 방지하는 비즈니스 로직이 올바르게 동작하는지 검증해야 함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(favoriteRepository.existsByUserAndPlace(user, place)).willReturn(true);

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> favoriteService.addFavorite(user.getEmail(), place.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Already favorited");
        }

        @Test
        @DisplayName("사용자를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_사용자_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 사용자가 즐겨찾기를 시도하는 비정상적인 경우를 차단하는지 확인하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> favoriteService.addFavorite(user.getEmail(), place.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
            // DB에 저장 시도가 없었는지 확인
            verify(favoriteRepository, never()).save(any());
        }

        @Test
        @DisplayName("장소를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_장소_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 장소를 즐겨찾기하려는 비정상적인 경우를 차단하는지 확인하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> favoriteService.addFavorite(user.getEmail(), 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Place not found");
            // DB에 저장 시도가 없었는지 확인
            verify(favoriteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("removeFavorite (즐겨찾기 삭제) 테스트")
    class Describe_removeFavorite {

        private Favorite favorite;

        @BeforeEach
        void setUp() {
            favorite = Favorite.builder()
                    .id(1L)
                    .user(user)
                    .place(place)
                    .build();
        }

        @Test
        @DisplayName("정상적인 요청 시, 즐겨찾기를 성공적으로 삭제한다")
        void 성공_즐겨찾기_삭제() {
            // 준비 (Arrange)
            // 왜? 사용자가 자신의 즐겨찾기 항목을 삭제하는 핵심 기능이 정상 동작하는지 검증하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(favoriteRepository.findByUserAndPlace(user, place)).willReturn(Optional.of(favorite));

            // 실행 (Act)
            favoriteService.removeFavorite(user.getEmail(), place.getId());

            // 검증 (Assert)
            verify(favoriteRepository, times(1)).delete(favorite);
        }

        @Test
        @DisplayName("삭제할 즐겨찾기 항목을 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_즐겨찾기_항목_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 데이터를 삭제하려는 잘못된 요청을 올바르게 처리하는지 검증해야 함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(favoriteRepository.findByUserAndPlace(user, place)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> favoriteService.removeFavorite(user.getEmail(), place.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Favorite not found");
        }

        @Test
        @DisplayName("사용자를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_사용자_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 사용자가 즐겨찾기 삭제를 시도하는 경우를 차단하는지 확인하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> favoriteService.removeFavorite(user.getEmail(), place.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
            // DB 삭제 시도가 없었는지 확인
            verify(favoriteRepository, never()).delete(any());
        }

        @Test
        @DisplayName("장소를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_장소_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 장소에 대한 즐겨찾기 삭제를 시도하는 경우를 차단하는지 확인하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> favoriteService.removeFavorite(user.getEmail(), 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Place not found");
            // DB 삭제 시도가 없었는지 확인
            verify(favoriteRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getFavorites (즐겨찾기 목록 조회) 테스트")
    class Describe_getFavorites {

        @Test
        @DisplayName("사용자의 즐겨찾기 목록을 정상적으로 조회한다")
        void 성공_목록_조회() {
            // 준비 (Arrange)
            // 왜? 특정 사용자의 즐겨찾기 목록을 DTO로 변환하여 올바르게 반환하는지 검증하기 위함.
            Place place2 = Place.builder().id(2L).roadAddress("서울 다른 주소").build();
            Favorite favorite1 = Favorite.builder().id(1L).user(user).place(place).build();
            Favorite favorite2 = Favorite.builder().id(2L).user(user).place(place2).build();

            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(favoriteRepository.findByUser(user)).willReturn(List.of(favorite1, favorite2));

            // 실행 (Act)
            List<PlaceResponseDto> result = favoriteService.getFavorites(user.getEmail());

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            // 모든 DTO의 isFavorited 필드는 true여야 함
            assertThat(result).allMatch(PlaceResponseDto::isFavorited);
            assertThat(result.get(0).getId()).isEqualTo(place.getId());
            assertThat(result.get(1).getId()).isEqualTo(place2.getId());
        }

        @Test
        @DisplayName("즐겨찾기한 장소가 없으면, 빈 리스트를 반환한다")
        void 성공_목록_없음() {
            // 준비 (Arrange)
            // 왜? 데이터가 없는 경계 조건에서 시스템이 오류 없이 정상적으로 빈 결과를 반환하는지 확인하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(favoriteRepository.findByUser(user)).willReturn(Collections.emptyList());

            // 실행 (Act)
            List<PlaceResponseDto> result = favoriteService.getFavorites(user.getEmail());

            // 검증 (Assert)
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("사용자를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_사용자_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 사용자의 즐겨찾기 목록을 조회하려는 시도를 차단하는지 확인하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> favoriteService.getFavorites(user.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }
    }
}
