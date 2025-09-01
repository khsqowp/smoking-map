package com.smoking_map.smoking_map.service.admin;

import com.smoking_map.smoking_map.domain.edit_request.EditRequest;
import com.smoking_map.smoking_map.domain.edit_request.EditRequestRepository;
import com.smoking_map.smoking_map.domain.edit_request.RequestStatus;
import com.smoking_map.smoking_map.domain.favorite.FavoriteRepository;
import com.smoking_map.smoking_map.domain.place.ImageInfo;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.review.ReviewRepository;
import com.smoking_map.smoking_map.service.s3.S3Uploader;
import com.smoking_map.smoking_map.web.dto.admin.AdminPlaceDto;
import com.smoking_map.smoking_map.web.dto.admin.PlaceUpdateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService - 장소 관리 단위 테스트")
class AdminServicePlaceManagementTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private EditRequestRepository editRequestRepository;

    @Mock
    private S3Uploader s3Uploader;

    private Place place1;
    private Place place2;

    @BeforeEach
    void setUp() {
        place1 = Place.builder()
                .id(1L)
                .roadAddress("서울 강남구")
                .imageInfos(new ArrayList<>()) // NullPointerException 방지를 위해 초기화
                .build();

        place2 = Place.builder()
                .id(2L)
                .roadAddress("서울 서초구")
                .imageInfos(new ArrayList<>()) // NullPointerException 방지를 위해 초기화
                .build();
    }

    @Nested
    @DisplayName("getAllPlaces (모든 장소 조회) 테스트")
    class Describe_getAllPlaces {

        @Test
        @DisplayName("검색어가 없을 때, 모든 장소 정보를 집계 데이터와 함께 반환한다")
        void 성공_모든_장소_조회() {
            // 준비 (Arrange)
            // 왜? 여러 Repository의 데이터를 조합하여 DTO를 생성하는 복잡한 조회 로직이 올바르게 동작하는지 검증하기 위함.
            given(placeRepository.findAll()).willReturn(List.of(place1, place2));
            // 각 장소의 집계 데이터를 Mocking
            given(favoriteRepository.countFavoritesByPlace()).willReturn(List.of(new Object[]{1L, 5L})); // place1의 즐겨찾기 5개
            given(reviewRepository.countReviewsByPlace()).willReturn(List.of(new Object[]{1L, 10L})); // place1의 리뷰 10개
            given(editRequestRepository.findAllByStatus(RequestStatus.PENDING)).willReturn(List.of(EditRequest.builder().place(place2).build())); // place2의 수정요청 1개

            // 실행 (Act)
            List<AdminPlaceDto> result = adminService.getAllPlaces(null);

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            AdminPlaceDto dto1 = result.stream().filter(dto -> dto.getId().equals(1L)).findFirst().get();
            assertThat(dto1.getFavoriteCount()).isEqualTo(5);
            assertThat(dto1.getReviewCount()).isEqualTo(10);
            assertThat(dto1.getPendingRequestCount()).isEqualTo(0);

            AdminPlaceDto dto2 = result.stream().filter(dto -> dto.getId().equals(2L)).findFirst().get();
            assertThat(dto2.getFavoriteCount()).isEqualTo(0);
            assertThat(dto2.getReviewCount()).isEqualTo(0);
            assertThat(dto2.getPendingRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("검색어가 있을 때, 해당 검색어를 포함하는 장소만 반환한다")
        void 성공_검색어로_장소_조회() {
            // 준비 (Arrange)
            // 왜? 검색 조건이 포함된 조회 기능이 정상적으로 동작하는지 검증하기 위함.
            given(placeRepository.findByRoadAddressContaining("강남")).willReturn(List.of(place1));
            // 검색 결과가 1개이므로, 집계 데이터도 1개에 대해서만 Mocking
            given(favoriteRepository.countFavoritesByPlace()).willReturn(List.of(new Object[]{1L, 5L}));
            given(reviewRepository.countReviewsByPlace()).willReturn(List.of());
            given(editRequestRepository.findAllByStatus(RequestStatus.PENDING)).willReturn(List.of());

            // 실행 (Act)
            List<AdminPlaceDto> result = adminService.getAllPlaces("강남");

            // 검증 (Assert)
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getFavoriteCount()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("updatePlaceDescription (장소 설명 수정) 테스트")
    class Describe_updatePlaceDescription {

        @Test
        @DisplayName("장소 설명을 수정하면, 설명이 변경되고 관련된 수정 요청 상태가 'REVIEWED'로 변경된다")
        void 성공_설명_수정_및_요청_상태_변경() {
            // 준비 (Arrange)
            // 왜? 핵심 수정 기능과 더불어, 관련된 데이터(수정 요청)의 상태를 연쇄적으로 변경하는 비즈니스 로직을 검증하기 위함.
            PlaceUpdateRequestDto requestDto = new PlaceUpdateRequestDto("새로운 설명");
            EditRequest pendingRequest = EditRequest.builder().status(RequestStatus.PENDING).build();
            given(placeRepository.findById(1L)).willReturn(Optional.of(place1));
            given(editRequestRepository.findAllByPlaceAndStatus(place1, RequestStatus.PENDING)).willReturn(List.of(pendingRequest));

            // 실행 (Act)
            adminService.updatePlaceDescription(1L, requestDto);

            // 검증 (Assert)
            assertThat(place1.getDescription()).isEqualTo("새로운 설명");
            assertThat(pendingRequest.getStatus()).isEqualTo(RequestStatus.REVIEWED);
        }

        @Test
        @DisplayName("존재하지 않는 장소의 설명 수정을 시도하면, IllegalArgumentException이 발생한다")
        void 실패_장소_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 데이터에 대한 수정 시도를 차단하는지 검증하기 위함.
            PlaceUpdateRequestDto requestDto = new PlaceUpdateRequestDto("새로운 설명");
            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> adminService.updatePlaceDescription(999L, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 장소가 없습니다.");
        }
    }

    @Nested
    @DisplayName("deletePlace (장소 삭제) 테스트")
    class Describe_deletePlace {

        @Test
        @DisplayName("장소를 삭제하면, DB에서 삭제되고 연관된 S3 이미지도 모두 삭제된다")
        void 성공_장소_및_S3이미지_삭제() {
            // 준비 (Arrange)
            // 왜? 데이터 삭제 시, 외부 시스템(S3)에 저장된 파일까지 연쇄적으로 삭제되는 중요한 로직을 검증하기 위함.
            place1.addImageInfo(ImageInfo.builder().imageUrl("url1").build());
            place1.addImageInfo(ImageInfo.builder().imageUrl("url2").build());
            given(placeRepository.findById(1L)).willReturn(Optional.of(place1));

            // 실행 (Act)
            adminService.deletePlace(1L);

            // 검증 (Assert)
            verify(placeRepository, times(1)).delete(place1);
            // S3 삭제가 2번 호출되었는지 확인
            verify(s3Uploader, times(2)).delete(anyString());
            verify(s3Uploader, times(1)).delete("url1");
            verify(s3Uploader, times(1)).delete("url2");
        }
    }

    @Nested
    @DisplayName("deletePlaceImage (장소 이미지 삭제) 테스트")
    class Describe_deletePlaceImage {

        @Test
        @DisplayName("특정 이미지를 삭제하면, 장소의 이미지 목록에서 제거되고 S3에서도 삭제된다")
        void 성공_이미지_삭제() {
            // 준비 (Arrange)
            // 왜? 장소의 하위 데이터(이미지)를 개별적으로 삭제하고, 외부 시스템(S3)과 상태를 동기화하는 로직을 검증하기 위함.
            ImageInfo imageInfo = ImageInfo.builder().id(10L).imageUrl("url_to_delete").build();
            place1.getImageInfos().add(imageInfo);
            given(placeRepository.findById(1L)).willReturn(Optional.of(place1));

            // 실행 (Act)
            adminService.deletePlaceImage(1L, "url_to_delete");

            // 검증 (Assert)
            assertThat(place1.getImageInfos()).isEmpty();
            verify(s3Uploader, times(1)).delete("url_to_delete");
        }

        @Test
        @DisplayName("삭제하려는 이미지를 찾을 수 없으면, IllegalArgumentException이 발생한다")
        void 실패_이미지_없음() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 하위 데이터를 삭제하려는 시도를 차단하는지 검증하기 위함.
            given(placeRepository.findById(1L)).willReturn(Optional.of(place1));

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> adminService.deletePlaceImage(1L, "non_existent_url"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 이미지를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("setRepresentativeImage (대표 이미지 설정) 테스트")
    class Describe_setRepresentativeImage {

        @Test
        @DisplayName("특정 이미지를 대표로 설정하면, 해당 이미지의 representative 필드가 true가 되고 나머지는 false가 된다")
        void 성공_대표_이미지_설정() {
            // 준비 (Arrange)
            // 왜? 여러 하위 데이터 중 하나만 특정 상태(대표)를 갖도록 보장하는 로직을 검증하기 위함.
            ImageInfo image1 = ImageInfo.builder().id(10L).imageUrl("url1").representative(true).build();
            ImageInfo image2 = ImageInfo.builder().id(20L).imageUrl("url2").representative(false).build();
            place1.getImageInfos().addAll(List.of(image1, image2));
            given(placeRepository.findById(1L)).willReturn(Optional.of(place1));

            // 실행 (Act)
            adminService.setRepresentativeImage(1L, 20L); // image2를 대표로 설정

            // 검증 (Assert)
            assertThat(image1.isRepresentative()).isFalse();
            assertThat(image2.isRepresentative()).isTrue();
        }
    }
}
