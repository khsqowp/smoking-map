package com.smoking_map.smoking_map.config.auth;

import com.smoking_map.smoking_map.config.auth.dto.OAuthAttributes;
import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.domain.user.Role; // Role import 추가
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // @Value import 추가
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    // application.yml에서 관리자 이메일 주소를 읽어오기 위한 설정
    @Value("${admin.email}")
    private String adminEmail;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);

        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        // [수정된 로직]
        // 1. 이메일로 사용자를 찾습니다.
        User user = userRepository.findByEmail(attributes.getEmail())
                // 2. 이미 존재하는 사용자이면 이름과 사진을 업데이트합니다.
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                // 3. 존재하지 않는 사용자이면 새로 생성합니다.
                .orElse(attributes.toEntity());

        // 4. 관리자 이메일과 일치하는지 확인하고, 맞다면 역할을 ADMIN으로 변경합니다.
        if (user.getEmail().equals(adminEmail)) {
            user.updateRole(Role.ADMIN);
        }

        // 5. 최종적으로 데이터베이스에 저장합니다.
        return userRepository.save(user);
    }
}