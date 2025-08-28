
package com.smoking_map.smoking_map.config.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        // --- 디버깅 로그 시작 ---
        log.info("--- OAuth2SuccessHandler가 받은 요청 상세 정보 ---");
        log.info("Request Scheme: {}", request.getScheme()); // http 인지 https 인지
        log.info("Request Server Name: {}", request.getServerName()); // 도메인 이름
        log.info("Request Server Port: {}", request.getServerPort()); // 포트

        // Nginx가 보낸 X-Forwarded 헤더들을 모두 출력합니다.
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            if (headerName.toLowerCase().startsWith("x-forwarded")) {
                log.info("Header {}: {}", headerName, request.getHeader(headerName));
            }
        });
        // --- 디버깅 로그 끝 ---

        String targetUrl = "https://smokingmap.duckdns.org";
        log.info("최종 리디렉션 시도 주소: {}", targetUrl);
        response.sendRedirect(targetUrl);
    }
}
