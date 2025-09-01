package com.smoking_map.smoking_map.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoking_map.smoking_map.config.auth.SecurityConfig;
import com.smoking_map.smoking_map.service.place.PlaceService;
import com.smoking_map.smoking_map.web.dto.PlaceResponseDto;
import com.smoking_map.smoking_map.web.dto.PlaceSaveRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// SecurityConfig를 스캔 대상에 포함하되, 커스텀 설정은 제외
@WebMvcTest(controllers = PlaceApiController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@DisplayName("PlaceApiController 통합 테스트")
class PlaceApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // Controller가 의존하는 Service를 Mock으로 만듦
    private PlaceService placeService;

    @Nested
    @DisplayName("GET /api/v1/places/{id} (장소 단건 조회) 테스트")
    class Describe_findById {

        @Test
        @WithMockUser // 인증된 사용자를 가정
        @DisplayName("존재하는 ID로 조회하면, 200 OK와 함께 장소 정보를 반환한다")
        void 성공_장소_단건_조회() throws Exception {
            // 준비 (Arrange)
            // 왜? 특정 장소를 조회하는 핵심 GET API가 서비스 계층의 응답을 받아 정상적으로 JSON으로 변환하여 반환하는지 검증하기 위함.
            Long placeId = 1L;
            PlaceResponseDto responseDto = new PlaceResponseDto(); // 필요한 필드 설정
            given(placeService.findById(placeId)).willReturn(responseDto);

            // 실행 및 검증 (Act & Assert)
            mvc.perform(get("/api/v1/places/{id}", placeId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(responseDto.getId())); // DTO 필드 검증
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 ID로 조회하면, 404 Not Found를 반환한다")
        void 실패_존재하지_않는_ID() throws Exception {
            // 준비 (Arrange)
            // 왜? 서비스 계층에서 발생한 예외(데이터 없음)를 Controller가 적절한 HTTP 상태 코드(404)로 변환하여 클라이언트에 응답하는지 검증하기 위함.
            Long nonExistentId = 999L;
            given(placeService.findById(nonExistentId)).willThrow(new IllegalArgumentException("해당 장소가 없습니다."));

            // 실행 및 검증 (Act & Assert)
            mvc.perform(get("/api/v1/places/{id}", nonExistentId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/places (장소 등록) 테스트")
    class Describe_save {

        @Test
        @WithMockUser(roles = "USER") // USER 권한을 가진 인증된 사용자를 가정
        @DisplayName("인증된 사용자가 유효한 데이터로 장소 등록을 요청하면, 200 OK와 함께 생성된 ID를 반환한다")
        void 성공_장소_등록() throws Exception {
            // 준비 (Arrange)
            // 왜? multipart/form-data 요청을 컨트롤러가 올바르게 파싱하고, 서비스 계층으로 DTO와 파일을 전달하는지 검증하기 위함.
            Long newPlaceId = 1L;
            PlaceSaveRequestDto requestDto = PlaceSaveRequestDto.builder()
                    .latitude(BigDecimal.valueOf(37.5))
                    .longitude(BigDecimal.valueOf(127.5))
                    .description("테스트")
                    .build();
            MockMultipartFile jsonFile = new MockMultipartFile("requestDto", "", "application/json", objectMapper.writeValueAsBytes(requestDto));
            MockMultipartFile imageFile = new MockMultipartFile("images", "image.jpg", "image/jpeg", "image_content".getBytes());

            given(placeService.save(any(PlaceSaveRequestDto.class), any(List.class))).willReturn(newPlaceId);

            // 실행 및 검증 (Act & Assert)
            mvc.perform(multipart("/api/v1/places")
                            .file(jsonFile)
                            .file(imageFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().string(String.valueOf(newPlaceId)));

            verify(placeService).save(any(PlaceSaveRequestDto.class), any(List.class));
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 장소 등록을 요청하면, 401 Unauthorized를 반환한다")
        void 실패_인증되지_않은_사용자() throws Exception {
            // 준비 (Arrange)
            // 왜? 인증이 필요한 API에 대해 Spring Security가 올바르게 요청을 차단하는지 검증하기 위함.
            PlaceSaveRequestDto requestDto = PlaceSaveRequestDto.builder().build();
            MockMultipartFile jsonFile = new MockMultipartFile("requestDto", "", "application/json", objectMapper.writeValueAsBytes(requestDto));

            // 실행 및 검증 (Act & Assert)
            mvc.perform(multipart("/api/v1/places").file(jsonFile))
                    .andExpect(status().isUnauthorized());
        }
    }
    
    @Nested
    @DisplayName("POST /api/v1/places/{id}/view (조회수 증가) 테스트")
    class Describe_increaseViewCount {

        @Test
        @WithMockUser
        @DisplayName("존재하는 장소의 조회수 증가를 요청하면, 200 OK를 반환한다")
        void 성공_조회수_증가() throws Exception {
            // 준비 (Arrange)
            // 왜? 특정 장소의 상태를 변경하는 POST 요청이 정상적으로 처리되는지 검증하기 위함.
            Long placeId = 1L;
            // placeService.increaseViewCount는 void를 반환하므로, given()은 필요 없음.

            // 실행 및 검증 (Act & Assert)
            mvc.perform(post("/api/v1/places/{id}/view", placeId))
                    .andExpect(status().isOk());

            // 서비스 메서드가 정확한 ID로 호출되었는지 검증
            verify(placeService).increaseViewCount(placeId);
        }
    }
}
