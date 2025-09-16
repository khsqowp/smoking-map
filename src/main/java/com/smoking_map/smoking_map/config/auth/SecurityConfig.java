package com.smoking_map.smoking_map.config.auth;

import com.smoking_map.smoking_map.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/places/*/view")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**", "/api/v1/user", "/api/v1/geocode", "/api/v1/announcements/active").permitAll()
                        // --- ▼▼▼ [수정] GET /api/v1/places/** 로 search도 포함되므로 별도 추가 불필요 ▼▼▼ ---
                        .requestMatchers(HttpMethod.GET, "/api/v1/places/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{id}/view").permitAll()
                        // --- ▼▼▼ [추가] 리뷰 API 권한 설정 ▼▼▼ ---
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{placeId}/reviews").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/{reviewId}").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        // --- ▲▲▲ [추가] 리뷰 API 권한 설정 ▲▲▲ ---
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{placeId}/favorite").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/places/{placeId}/favorite").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/favorites").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        // --- ▲▲▲ [추가] 즐겨찾기 API 권한 설정 ▲▲▲ ---
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{placeId}/report").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{placeId}/edit-requests").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/places").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/places/{id}/images").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.MANAGER.name())
                        .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated()
                )
                //배포용
//                .logout(logout -> logout
//                        .logoutSuccessUrl("https://smokingmap.duckdns.org")                )
//                .oauth2Login(oauth2 -> oauth2
//                        .defaultSuccessUrl("https://smokingmap.duckdns.org", true)

                //내부 테스트용
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/", true)


                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                );

        return http.build();
    }
}