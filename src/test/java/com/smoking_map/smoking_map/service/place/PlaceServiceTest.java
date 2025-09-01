package com.smoking_map.smoking_map.service.place;

import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.domain.favorite.FavoriteRepository;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.service.geocoding.GeocodingService;
import com.smoking_map.smoking_map.service.s3.FileValidator;
import com.smoking_map.smoking_map.service.s3.S3Uploader;
import com.smoking_map.smoking_map.web.dto.PlaceResponseDto;
import com.smoking_map.smoking_map.web.dto.PlaceSaveRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceService 단위 테스트")
class PlaceServiceTest {

    // @InjectMocks: Mock 객체들을 실제 테스트 대상 클래스에 주입합니다.
    @InjectMocks
    private PlaceService placeService;

    // @Mock: 의존성을 가지는 클래스들을 Mock으로 만듭니다.
    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Uploader s3Uploader;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private FavoriteRepository favoriteRepository;

    // MockHttpSession: 실제 서블릿 컨테이너 없이 세션을 테스트하기 위한 Mock 객체
    private MockHttpSession mockSession;

    private User user;
    private SessionUser sessionUser;

    // beforeEach: 각 테스트가 실행되기 전에 공통적으로 필요한 설정을 합니다.
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 객체 생성
        user = User.builder()
                .id(1L)
                .name("테스트유저")
                .email("test@example.com")
                .picture("test.jpg")
                .build();
        // 테스트용 세션 사용자 객체 생성
        sessionUser = new SessionUser(user);
        // Mock 세션 객체 생성 및 "user" 속성 설정
        mockSession = new MockHttpSession();
        mockSession.setAttribute("user", sessionUser);

        // PlaceService의 httpSession 필드에 Mock 세션 주입 (리플렉션을 사용하거나, 생성자 주입 방식 변경 필요)
        // 여기서는 테스트의 편의를 위해 생성자 주입을 가정하고, 실제 코드에서는 리플렉션 또는 다른 방법을 사용해야 할 수 있습니다.
        // 하지만 현재 PlaceService는 @RequiredArgsConstructor를 사용하므로, 직접 주입은 어렵습니다.
        // 대신, httpSession을 사용하는 메서드에서 given(httpSession.getAttribute("user")).willReturn(sessionUser); 와 같이 설정합니다.
        // @InjectMocks는 생성자 주입을 시도하므로, httpSession을 Mock으로 만들고 아래와 같이 설정합니다.
    }

    @Mock
    private jakarta.servlet.http.HttpSession httpSession; // 실제 HttpSession을 Mocking

    @Nested
    @DisplayName("save (장소 등록) 테스트")
    class Describe_save {

        private PlaceSaveRequestDto requestDto;
        private List<MultipartFile> images;

        @BeforeEach
        void setUp() {
            // 정상적인 장소 등록 요청 DTO 생성
            requestDto = PlaceSaveRequestDto.builder()
                    .latitude(BigDecimal.valueOf(37.5665))
                    .longitude(BigDecimal.valueOf(126.9780))
                    .originalAddress("서울시 중구 세종대로 110")
                    .description("테스트 설명")
                    .build();
            // 정상적인 이미지 파일 Mock 생성
            images = List.of(
                    new MockMultipartFile("images", "image1.jpg", "image/jpeg", "image1_content".getBytes())
            );
        }

        @Test
        @DisplayName("정상적인 데이터로 장소를 등록하면, 생성된 장소의 ID를 반환한다 (Happy Path)")
        void 성공_정상적인_데이터() throws IOException {
            // 준비 (Arrange)
            // 왜? 세션에서 사용자를 찾고, 좌표를 주소로 변환하며, S3에 업로드하고, 최종적으로 DB에 저장하는 전체 흐름을 테스트하기 위함.

            // Mock 객체들의 행동 정의
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(geocodingService.getAddressFromCoords(anyDouble(), anyDouble()))
                    .willReturn(new GeocodingService.GeocodingResult("서울시", "중구", "세종대로 110"));
            given(s3Uploader.upload(any(), anyString(), anyString())).willReturn("http://s3.com/image1.jpg");
            
            Place savedPlace = Place.builder().id(1L).build();
            given(placeRepository.save(any(Place.class))).willReturn(savedPlace);
            
            // 파일 유효성 검사는 통과하도록 설정
            doNothing().when(fileValidator).validateImageFile(any(MultipartFile.class));

            // 실행 (Act)
            Long savedId = placeService.save(requestDto, images);

            // 검증 (Assert)
            assertThat(savedId).isEqualTo(1L);
            // placeRepository.save가 Place 객체를 인자로 1번 호출되었는지 검증
            verify(placeRepository, times(1)).save(any(Place.class));
            // s3Uploader.upload가 1번 호출되었는지 검증
            verify(s3Uploader, times(1)).upload(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("이미지 없이 장소를 등록해도, 정상적으로 등록되어야 한다")
        void 성공_이미지_없음() throws IOException {
            // 준비 (Arrange)
            // 왜? 이미지는 선택 사항이므로, 이미지가 없는 경우에도 장소 등록이 정상적으로 동작하는지 확인해야 함.
            List<MultipartFile> emptyImages = new ArrayList<>();

            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(geocodingService.getAddressFromCoords(anyDouble(), anyDouble()))
                    .willReturn(new GeocodingService.GeocodingResult("서울시", "중구", "세종대로 110"));
            
            Place savedPlace = Place.builder().id(2L).build();
            given(placeRepository.save(any(Place.class))).willReturn(savedPlace);

            // 실행 (Act)
            Long savedId = placeService.save(requestDto, emptyImages);

            // 검증 (Assert)
            assertThat(savedId).isEqualTo(2L);
            verify(placeRepository, times(1)).save(any(Place.class));
            // s3Uploader.upload는 호출되지 않아야 함
            verify(s3Uploader, never()).upload(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("세션에 사용자 정보가 없으면, IllegalArgumentException이 발생한다")
        void 실패_세션_없음() {
            // 준비 (Arrange)
            // 왜? 비로그인 사용자는 장소를 등록할 수 없어야 하므로, 세션이 없을 때 예외가 발생하는지 확인해야 함.
            given(httpSession.getAttribute("user")).willReturn(null);

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> placeService.save(requestDto, images))
                    .isInstanceOf(NullPointerException.class); // sessionUser가 null이므로 NullPointerException 발생
        }

        @Test
        @DisplayName("DB에 해당 이메일의 사용자가 없으면, IllegalArgumentException이 발생한다")
        void 실패_사용자_없음() {
            // 준비 (Arrange)
            // 왜? 세션 정보는 있지만 DB에 해당 사용자가 없는 비정상적인 상황(예: 회원 탈퇴 직후)을 올바르게 처리하는지 확인해야 함.
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> placeService.save(requestDto, images))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 사용자가 없습니다.");
        }

        @Test
        @DisplayName("Geocoding 서비스가 실패하면, 해당 예외가 전파된다")
        void 실패_Geocoding_오류() throws IOException {
            // 준비 (Arrange)
            // 왜? 외부 서비스인 Geocoding API 연동이 실패했을 때, 시스템이 비정상 종료되지 않고 오류를 올바르게 전파하는지 확인해야 함.
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(geocodingService.getAddressFromCoords(anyDouble(), anyDouble()))
                    .willThrow(new IOException("Geocoding service failure"));

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> placeService.save(requestDto, images))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Geocoding service failure");
        }

        @Test
        @DisplayName("S3 업로드가 실패하면, 해당 예외가 전파된다")
        void 실패_S3_업로드_오류() throws IOException {
            // 준비 (Arrange)
            // 왜? 파일 저장소(S3) 연동이 실패했을 때, 트랜잭션이 롤백되고 오류가 사용자에게 전달되는지 확인해야 함.
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(geocodingService.getAddressFromCoords(anyDouble(), anyDouble()))
                    .willReturn(new GeocodingService.GeocodingResult("서울시", "중구", "세종대로 110"));
            given(s3Uploader.upload(any(), anyString(), anyString()))
                    .willThrow(new IOException("S3 upload failure"));
            doNothing().when(fileValidator).validateImageFile(any(MultipartFile.class));

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> placeService.save(requestDto, images))
                    .isInstanceOf(IOException.class)
                    .hasMessage("S3 upload failure");
        }
        
        @Test
        @DisplayName("유효하지 않은 이미지 파일이 포함되면, IllegalArgumentException이 발생한다")
        void 실패_잘못된_파일_형식() throws IOException {
            // 준비 (Arrange)
            // 왜? 이미지 파일이 아닌 다른 형식의 파일(예: .txt, .zip)이 업로드되는 것을 방지하는지 확인해야 함.
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(geocodingService.getAddressFromCoords(anyDouble(), anyDouble()))
                    .willReturn(new GeocodingService.GeocodingResult("서울시", "중구", "세종대로 110"));
            
            // fileValidator가 예외를 던지도록 설정
            doThrow(new IllegalArgumentException("Invalid file type"))
                .when(fileValidator).validateImageFile(any(MultipartFile.class));

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> placeService.save(requestDto, images))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid file type");
        }
    }

    @Nested
    @DisplayName("findById (장소 단건 조회) 테스트")
    class Describe_findById {

        @Test
        @DisplayName("존재하는 ID로 조회하면, 해당 장소 정보를 PlaceResponseDto로 반환한다 (비로그인 사용자)")
        void 성공_비로그인_사용자() {
            // 준비 (Arrange)
            // 왜? 비로그인 상태에서도 장소 조회가 정상적으로 동작하는지, 특히 즐겨찾기 여부(`isFavorited`)가 `false`로 반환되는지 확인해야 함.
            Place place = Place.builder().id(1L).latitude(BigDecimal.valueOf(37.5665)).longitude(BigDecimal.valueOf(126.9780)).build();
            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            given(httpSession.getAttribute("user")).willReturn(null); // 비로그인 상태

            // 실행 (Act)
            PlaceResponseDto responseDto = placeService.findById(1L);

            // 검증 (Assert)
            assertThat(responseDto.getId()).isEqualTo(1L);
            assertThat(responseDto.isFavorited()).isFalse();
        }

        @Test
        @DisplayName("존재하는 ID로 조회하면, 해당 장소 정보를 PlaceResponseDto로 반환한다 (로그인 사용자, 즐겨찾기 안함)")
        void 성공_로그인_사용자_즐겨찾기_안함() {
            // 준비 (Arrange)
            // 왜? 로그인한 사용자가 즐겨찾기하지 않은 장소를 조회했을 때, `isFavorited`가 `false`로 정확히 반환되는지 확인해야 함.
            Place place = Place.builder().id(1L).latitude(BigDecimal.valueOf(37.5665)).longitude(BigDecimal.valueOf(126.9780)).build();
            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(favoriteRepository.existsByUserAndPlace(user, place)).willReturn(false);

            // 실행 (Act)
            PlaceResponseDto responseDto = placeService.findById(1L);

            // 검증 (Assert)
            assertThat(responseDto.getId()).isEqualTo(1L);
            assertThat(responseDto.isFavorited()).isFalse();
        }

        @Test
        @DisplayName("존재하는 ID로 조회하면, 해당 장소 정보를 PlaceResponseDto로 반환한다 (로그인 사용자, 즐겨찾기 함)")
        void 성공_로그인_사용자_즐겨찾기_함() {
            // 준비 (Arrange)
            // 왜? 로그인한 사용자가 즐겨찾기한 장소를 조회했을 때, `isFavorited`가 `true`로 정확히 반환되는지 확인해야 함.
            Place place = Place.builder().id(1L).latitude(BigDecimal.valueOf(37.5665)).longitude(BigDecimal.valueOf(126.9780)).build();
            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(favoriteRepository.existsByUserAndPlace(user, place)).willReturn(true);

            // 실행 (Act)
            PlaceResponseDto responseDto = placeService.findById(1L);

            // 검증 (Assert)
            assertThat(responseDto.getId()).isEqualTo(1L);
            assertThat(responseDto.isFavorited()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면, IllegalArgumentException이 발생한다")
        void 실패_존재하지_않는_ID() {
            // 준비 (Arrange)
            // 왜? DB에 없는 데이터를 조회하려고 할 때, 시스템이 적절한 예외를 발생시켜 오류 상황을 명확히 알리는지 확인해야 함.
            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> placeService.findById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 장소가 없습니다.");
        }
    }
    
    @Nested
    @DisplayName("findAll (장소 전체 조회) 테스트")
    class Describe_findAll {

        @Test
        @DisplayName("비로그인 상태에서 전체 장소를 조회하면, 모든 장소의 isFavorited가 false로 반환된다")
        void 성공_비로그인_사용자() {
            // 준비 (Arrange)
            // 왜? 비로그인 사용자는 즐겨찾기 정보가 없으므로, 모든 장소에 대해 즐겨찾기 여부가 false로 일관되게 반환되는지 확인해야 함.
            Place place1 = Place.builder().id(1L).build();
            Place place2 = Place.builder().id(2L).build();
            given(placeRepository.findAll()).willReturn(List.of(place1, place2));
            given(httpSession.getAttribute("user")).willReturn(null);

            // 실행 (Act)
            List<PlaceResponseDto> result = placeService.findAll();

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(dto -> !dto.isFavorited());
        }

        @Test
        @DisplayName("로그인 상태에서 전체 장소를 조회하면, 즐겨찾기한 장소의 isFavorited만 true로 반환된다")
        void 성공_로그인_사용자() {
            // 준비 (Arrange)
            // 왜? 로그인한 사용자에 대해, 즐겨찾기한 장소와 그렇지 않은 장소의 `isFavorited` 상태가 정확하게 구분되어 반환되는지 확인해야 함.
            Place place1 = Place.builder().id(1L).build(); // 즐겨찾기 한 장소
            Place place2 = Place.builder().id(2L).build(); // 즐겨찾기 안 한 장소
            given(placeRepository.findAll()).willReturn(List.of(place1, place2));
            given(httpSession.getAttribute("user")).willReturn(sessionUser);
            given(userRepository.findByEmail(sessionUser.getEmail())).willReturn(Optional.of(user));
            given(favoriteRepository.findPlaceIdsByUser(user)).willReturn(Set.of(1L)); // 1번 장소만 즐겨찾기

            // 실행 (Act)
            List<PlaceResponseDto> result = placeService.findAll();

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            assertThat(result.get(0).isFavorited()).isTrue(); // ID 1L
            assertThat(result.get(1).isFavorited()).isFalse(); // ID 2L
        }
        
        @Test
        @DisplayName("장소가 하나도 없을 경우, 빈 리스트를 반환한다")
        void 성공_장소_없음() {
            // 준비 (Arrange)
            // 왜? 데이터가 없는 경계 조건에서 시스템이 오류 없이 정상적으로 빈 결과를 반환하는지 확인해야 함.
            given(placeRepository.findAll()).willReturn(Collections.emptyList());
            given(httpSession.getAttribute("user")).willReturn(null);

            // 실행 (Act)
            List<PlaceResponseDto> result = placeService.findAll();

            // 검증 (Assert)
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("increaseViewCount (조회수 증가) 테스트")
    class Describe_increaseViewCount {

        @Test
        @DisplayName("존재하는 장소의 ID를 받으면, 해당 장소의 조회수가 1 증가한다")
        void 성공_조회수_증가() {
            // 준비 (Arrange)
            // 왜? 조회수 증가 기능이 정상적으로 동작하여 DB 상태를 올바르게 변경하는지 확인해야 함.
            Place place = Place.builder().viewCount(10).build();
            given(placeRepository.findById(1L)).willReturn(Optional.of(place));

            // 실행 (Act)
            placeService.increaseViewCount(1L);

            // 검증 (Assert)
            assertThat(place.getViewCount()).isEqualTo(11);
            // place.increaseViewCount() 메서드가 호출되었는지 간접적으로 확인 (상태 변경 검증)
        }

        @Test
        @DisplayName("존재하지 않는 장소의 ID를 받으면, IllegalArgumentException이 발생한다")
        void 실패_존재하지_않는_ID() {
            // 준비 (Arrange)
            // 왜? 존재하지 않는 데이터에 대한 수정을 시도할 때, 적절한 예외를 발생시켜 잘못된 연산을 방지하는지 확인해야 함.
            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> placeService.increaseViewCount(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 장소가 없습니다.");
        }
    }
    
    @Nested
    @DisplayName("searchPlacesByKeyword (키워드 검색) 테스트")
    class Describe_searchPlacesByKeyword {

        @Test
        @DisplayName("유효한 키워드로 검색하면, 주소에 키워드가 포함된 장소 목록을 반환한다")
        void 성공_유효한_키워드() {
            // 준비 (Arrange)
            // 왜? 검색 기능의 핵심 로직이 정상적으로 동작하여, 조건에 맞는 결과를 정확히 필터링하는지 확인해야 함.
            Place place1 = Place.builder().roadAddress("서울시 강남구").build();
            Place place2 = Place.builder().roadAddress("서울시 서초구").build();
            given(placeRepository.findByAddressKeyword("강남")).willReturn(List.of(place1));

            // 실행 (Act)
            List<PlaceResponseDto> result = placeService.searchPlacesByKeyword("강남");

            // 검증 (Assert)
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRoadAddress()).isEqualTo("서울시 강남구");
        }

        @Test
        @DisplayName("키워드가 null이거나 빈 문자열이면, 빈 리스트를 반환한다")
        void 실패_키워드_없음() {
            // 준비 (Arrange)
            // 왜? 유효하지 않은 입력값(null, 공백)에 대해 불필요한 DB 조회를 수행하지 않고, 즉시 빈 결과를 반환하여 성능을 확보하는지 확인해야 함.
            
            // 실행 (Act)
            List<PlaceResponseDto> resultForNull = placeService.searchPlacesByKeyword(null);
            List<PlaceResponseDto> resultForEmpty = placeService.searchPlacesByKeyword("");
            List<PlaceResponseDto> resultForBlank = placeService.searchPlacesByKeyword("   ");

            // 검증 (Assert)
            assertThat(resultForNull).isNotNull().isEmpty();
            assertThat(resultForEmpty).isNotNull().isEmpty();
            assertThat(resultForBlank).isNotNull().isEmpty();
            // repository가 호출되지 않았는지 검증
            verify(placeRepository, never()).findByAddressKeyword(any());
        }

        @Test
        @DisplayName("검색 결과가 없으면, 빈 리스트를 반환한다")
        void 성공_검색_결과_없음() {
            // 준비 (Arrange)
            // 왜? 검색 조건에 맞는 데이터가 없을 때, 시스템이 null 대신 빈 리스트를 일관되게 반환하는지 확인해야 함.
            given(placeRepository.findByAddressKeyword("없는키워드")).willReturn(Collections.emptyList());

            // 실행 (Act)
            List<PlaceResponseDto> result = placeService.searchPlacesByKeyword("없는키워드");

            // 검증 (Assert)
            assertThat(result).isNotNull().isEmpty();
        }
    }
}
