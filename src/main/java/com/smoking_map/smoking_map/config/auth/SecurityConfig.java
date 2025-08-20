// src/main/java/com/smoking_map/smoking_map/config/auth/SecurityConfig.java
package com.smoking_map.smoking_map.config.auth;

import com.smoking_map.smoking_map.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**", "/api/v1/user", "/api/v1/geocode").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/places/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{id}/view").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{placeId}/report").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())

                        // --- ▼▼▼ [수정] 수정 요청 API 권한 추가 ▼▼▼ ---
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{placeId}/edit-requests").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())

                        .requestMatchers(HttpMethod.POST, "/api/v1/places").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{id}/images").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("http://localhost:3001")
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                );

        return http.build();
    }
}