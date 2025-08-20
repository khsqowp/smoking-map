// src/main/java/com/smoking_map/smoking_map/config/WebConfig.java
package com.smoking_map.smoking_map.config;

import com.smoking_map.smoking_map.config.auth.LoginUserArgumentResolver; // import 추가
import lombok.RequiredArgsConstructor; // import 추가
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver; // import 추가
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List; // import 추가

@RequiredArgsConstructor // 어노테이션 추가
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginUserArgumentResolver loginUserArgumentResolver; // Argument Resolver 주입

    // --- ▼▼▼ [수정] 메서드 추가 ▼▼▼ ---
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginUserArgumentResolver);
    }
    // --- ▲▲▲ [수정] 메서드 추가 ▲▲▲ ---

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3001")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // PATCH 추가
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}