// src/main/java/com/smoking_map/smoking_map/config/WebConfig.java
package com.smoking_map.smoking_map.config;

import com.smoking_map.smoking_map.config.auth.LoginUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean; // import 추가
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter; // import 추가
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginUserArgumentResolver loginUserArgumentResolver;

     // 리디렉션 문제를 해결하기 위한 Bean을 추가합니다.
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginUserArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 실제 서비스 도메인을 CORS 허용 목록에 추가합니다.
                .allowedOrigins("http://localhost:3000", "https://smokingmap.duckdns.org")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
