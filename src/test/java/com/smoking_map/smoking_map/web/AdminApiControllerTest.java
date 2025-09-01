package com.smoking_map.smoking_map.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoking_map.smoking_map.config.auth.SecurityConfig;
import com.smoking_map.smoking_map.domain.user.Role;
import com.smoking_map.smoking_map.service.admin.AdminService;
import com.smoking_map.smoking_map.web.dto.admin.UserRoleUpdateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = AdminApiController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@DisplayName("AdminApiController 통합 테스트")
class AdminApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @Nested
    @DisplayName("GET /api/v1/admin/users (사용자 목록 조회) 테스트")
    class Describe_getAllUsers {

        @Test
        @WithMockUser(roles = "ADMIN") // ADMIN 역할의 사용자로 요청
        @DisplayName("ADMIN 사용자가 요청하면, 200 OK와 함께 사용자 목록을 반환한다")
        void 성공_ADMIN_사용자() throws Exception {
            // 준비 (Arrange)
            // 왜? ADMIN 권한을 가진 사용자가 보호된 자원에 정상적으로 접근할 수 있는지 확인하기 위함.
            given(adminService.getAllUsers()).willReturn(Collections.emptyList());

            // 실행 및 검증 (Act & Assert)
            mvc.perform(get("/api/v1/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @WithMockUser(roles = "USER") // USER 역할의 사용자로 요청
        @DisplayName("USER 사용자가 요청하면, 403 Forbidden을 반환한다")
        void 실패_USER_사용자() throws Exception {
            // 준비 (Arrange)
            // 왜? ADMIN 권한이 없는 사용자가 보호된 자원에 접근하려 할 때, 권한 없음(403) 응답을 받는지 보안을 검증하기 위함.

            // 실행 및 검증 (Act & Assert)
            mvc.perform(get("/api/v1/admin/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 요청하면, 401 Unauthorized를 반환한다")
        void 실패_인증되지_않은_사용자() throws Exception {
            // 준비 (Arrange)
            // 왜? 익명 사용자가 보호된 자원에 접근하려 할 때, 인증 필요(401) 응답을 받는지 보안을 검증하기 위함.

            // 실행 및 검증 (Act & Assert)
            mvc.perform(get("/api/v1/admin/users"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/role (사용자 역할 변경) 테스트")
    class Describe_updateUserRole {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN 사용자가 요청하면, 200 OK를 반환한다")
        void 성공_ADMIN_사용자() throws Exception {
            // 준비 (Arrange)
            // 왜? ADMIN 권한으로 사용자의 역할을 변경하는 민감한 작업이 정상적으로 수행되는지 검증하기 위함.
            Long userId = 1L;
            UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto(Role.ADMIN.name());

            // 실행 및 검증 (Act & Assert)
            mvc.perform(patch("/api/v1/admin/users/{id}/role", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk());

            verify(adminService).updateUserRole(userId, requestDto);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("USER 사용자가 요청하면, 403 Forbidden을 반환한다")
        void 실패_USER_사용자() throws Exception {
            // 준비 (Arrange)
            // 왜? 일반 사용자가 다른 사용자의 권한을 변경하는 매우 민감한 작업을 시도할 때, 보안 정책이 올바르게 차단하는지 검증하기 위함.
            Long userId = 1L;
            UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto(Role.ADMIN.name());

            // 실행 및 검증 (Act & Assert)
            mvc.perform(patch("/api/v1/admin/users/{id}/role", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/places/{id} (장소 삭제) 테스트")
    class Describe_deletePlace {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN 사용자가 요청하면, 200 OK를 반환한다")
        void 성공_ADMIN_사용자() throws Exception {
            // 준비 (Arrange)
            // 왜? ADMIN 권한으로 장소 데이터를 삭제하는 기능이 정상적으로 수행되는지 검증하기 위함.
            Long placeId = 1L;

            // 실행 및 검증 (Act & Assert)
            mvc.perform(delete("/api/v1/admin/places/{id}", placeId))
                    .andExpect(status().isOk());

            verify(adminService).deletePlace(placeId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("USER 사용자가 요청하면, 403 Forbidden을 반환한다")
        void 실패_USER_사용자() throws Exception {
            // 준비 (Arrange)
            // 왜? 일반 사용자가 장소 데이터를 임의로 삭제하는 것을 보안 정책이 올바르게 차단하는지 검증하기 위함.
            Long placeId = 1L;

            // 실행 및 검증 (Act & Assert)
            mvc.perform(delete("/api/v1/admin/places/{id}", placeId))
                    .andExpect(status().isForbidden());
        }
    }
}
