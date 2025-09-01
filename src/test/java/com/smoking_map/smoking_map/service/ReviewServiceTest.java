package com.smoking_map.smoking_map.service;

import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.review.Review;
import com.smoking_map.smoking_map.domain.review.ReviewRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.ReviewRequestDto;
import com.smoking_map.smoking_map.web.dto.ReviewResponseDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlaceRepository placeRepository;

    private User user;
    private User otherUser;
    private Place place;
    private Review review;

    @BeforeEach
    void setUp() {
        // 테스트용 공통 객체 설정
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트유저")
                .build();

        otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .name("다른유저")
                .build();

        place = Place.builder()
                .id(1L)
                .build();

        review = Review.builder()
                .id(1L)
                .user(user)
                .place(place)
                .rating(5)
                .comment("최고에요")
                .build();
    }

    @Nested
    @DisplayName("createReview (리뷰 생성) 테스트")
    class Describe_createReview {

        private ReviewRequestDto requestDto;

        @BeforeEach
        void setUp() {
            requestDto = new ReviewRequestDto(5, "좋아요");
        }

        @Test
        @DisplayName("정상적인 요청 시, 리뷰를 생성하고 생성된 ID를 반환한다")
        void 성공_리뷰_생성() {
            // 준비 (Arrange)
            // 왜? 사용자와 장소가 모두 존재하고 요청 데이터가 유효할 때, 리뷰가 DB에 저장되고 장소의 통계가 업데이트되는 핵심 흐름을 검증하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(reviewRepository.save(any(Review.class))).willReturn(review);
            given(reviewRepository.findAverageRatingByPlace(place)).willReturn(4.5);
            given(reviewRepository.countByPlace(place)).willReturn(10L);

            // 실행 (Act)
            Long reviewId = reviewService.createReview(user.getEmail(), place.getId(), requestDto);

            // 검증 (Assert)
            assertThat(reviewId).isEqualTo(review.getId());
            // reviewRepository.save가 한 번 호출되었는지 검증
            verify(reviewRepository, times(1)).save(any(Review.class));
            // place.updateReviewStats가 호출되었는지 검증 (호출 후 평점과 리뷰 수가 변경되었는지 확인)
            verify(placeRepository, times(1)).save(place); // updateReviewStats는 place를 변경하고, 변경된 place는 저장되어야 함 (실제 코드에 따라 다름)
            assertThat(place.getAverageRating()).isEqualTo(4.5);
            assertThat(place.getReviewCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("사용자를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_사용자_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 사용자가 리뷰를 작성하려는 비정상적인 시도를 차단하는지 확인하기 위함.
            given(userRepository.findByEmail("nonexistent@example.com")).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> reviewService.createReview("nonexistent@example.com", place.getId(), requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("장소를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_장소_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 장소에 리뷰를 작성하려는 비정상적인 시도를 차단하는지 확인하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> reviewService.createReview(user.getEmail(), 999L, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Place not found");
        }
    }

    @Nested
    @DisplayName("deleteReview (리뷰 삭제) 테스트")
    class Describe_deleteReview {

        @Test
        @DisplayName("리뷰 작성자 본인이 삭제를 요청하면, 리뷰가 정상적으로 삭제된다")
        void 성공_본인_리뷰_삭제() {
            // 준비 (Arrange)
            // 왜? 사용자가 자신의 리뷰를 삭제하는 핵심 기능이 정상 동작하는지, 삭제 후 장소 통계가 올바르게 업데이트되는지 검증하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));
            // 마지막 리뷰가 삭제되는 상황을 가정
            given(reviewRepository.findAverageRatingByPlace(place)).willReturn(null);
            given(reviewRepository.countByPlace(place)).willReturn(0L);


            // 실행 (Act)
            reviewService.deleteReview(review.getId(), user.getEmail());

            // 검증 (Assert)
            // reviewRepository.delete가 한 번 호출되었는지 검증
            verify(reviewRepository, times(1)).delete(review);
            // 장소 통계가 0으로 초기화되었는지 검증
            assertThat(place.getAverageRating()).isEqualTo(0.0);
            assertThat(place.getReviewCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("리뷰 작성자가 아닌 다른 사용자가 삭제를 요청하면, IllegalStateException이 발생한다")
        void 실패_타인_리뷰_삭제() {
            // 준비 (Arrange)
            // 왜? 다른 사용자의 리뷰를 임의로 삭제할 수 없도록 하는 보안 로직이 올바르게 동작하는지 반드시 검증해야 함.
            given(userRepository.findByEmail(otherUser.getEmail())).willReturn(Optional.of(otherUser));
            given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> reviewService.deleteReview(review.getId(), otherUser.getEmail()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Not authorized to delete this review");
        }

        @Test
        @DisplayName("삭제할 리뷰를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_리뷰_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 리뷰를 삭제하려는 시도를 올바르게 차단하는지 확인하기 위함.
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(reviewRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> reviewService.deleteReview(999L, user.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Review not found");
        }
    }

    @Nested
    @DisplayName("getReviewsForPlace (특정 장소 리뷰 목록 조회) 테스트")
    class Describe_getReviewsForPlace {

        @Test
        @DisplayName("로그인한 사용자가 리뷰 목록을 조회하면, 본인 리뷰에 isMine=true가 표시된다")
        void 성공_로그인_사용자_조회() {
            // 준비 (Arrange)
            // 왜? 리뷰 목록에서 사용자가 자신의 리뷰를 쉽게 식별할 수 있도록 `isMine` 플래그가 정확하게 설정되는지 검증하기 위함.
            Review otherReview = Review.builder().id(2L).user(otherUser).place(place).comment("별로에요").rating(1).build();
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(reviewRepository.findByPlace(place)).willReturn(List.of(review, otherReview));

            // 실행 (Act)
            List<ReviewResponseDto> result = reviewService.getReviewsForPlace(place.getId(), user.getEmail());

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            // user가 작성한 리뷰(id:1)는 isMine이 true여야 함
            assertThat(result.stream().filter(r -> r.getReviewId().equals(review.getId())).findFirst().get().isMine()).isTrue();
            // otherUser가 작성한 리뷰(id:2)는 isMine이 false여야 함
            assertThat(result.stream().filter(r -> r.getReviewId().equals(otherReview.getId())).findFirst().get().isMine()).isFalse();
        }

        @Test
        @DisplayName("비로그인 사용자가 리뷰 목록을 조회하면, 모든 리뷰에 isMine=false가 표시된다")
        void 성공_비로그인_사용자_조회() {
            // 준비 (Arrange)
            // 왜? 비로그인 상태에서는 '내 리뷰'라는 개념이 없으므로, 모든 리뷰의 `isMine` 플래그가 일관되게 `false`로 반환되는지 확인하기 위함.
            Review otherReview = Review.builder().id(2L).user(otherUser).place(place).comment("별로에요").rating(1).build();
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(reviewRepository.findByPlace(place)).willReturn(List.of(review, otherReview));

            // 실행 (Act)
            List<ReviewResponseDto> result = reviewService.getReviewsForPlace(place.getId(), null); // userEmail을 null로 전달

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> !r.isMine());
        }

        @Test
        @DisplayName("리뷰가 없는 장소를 조회하면, 빈 리스트를 반환한다")
        void 성공_리뷰_없는_장소() {
            // 준비 (Arrange)
            // 왜? 리뷰가 없는 경계 조건에서 시스템이 오류 없이 정상적으로 빈 결과를 반환하는지 확인하기 위함.
            given(placeRepository.findById(place.getId())).willReturn(Optional.of(place));
            given(reviewRepository.findByPlace(place)).willReturn(List.of());

            // 실행 (Act)
            List<ReviewResponseDto> result = reviewService.getReviewsForPlace(place.getId(), user.getEmail());

            // 검증 (Assert)
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("장소를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_장소_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 장소의 리뷰를 조회하려는 잘못된 요청을 올바르게 처리하는지 확인하기 위함.
            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> reviewService.getReviewsForPlace(999L, user.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Place not found");
        }
    }
}
