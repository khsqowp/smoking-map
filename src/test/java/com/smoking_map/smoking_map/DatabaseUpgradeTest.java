
package com.smoking_map.smoking_map;

import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.activity_log.UserActivityLog;
import com.smoking_map.smoking_map.domain.activity_log.UserActivityLogRepository;
import com.smoking_map.smoking_map.domain.user.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // 필요한 경우 주석 해제

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import io.awspring.cloud.s3.S3Template; // S3Template을 MockBean으로 사용하기 위해 추가

@SpringBootTest // 전체 Spring 애플리케이션 컨텍스트를 로드합니다.
public class DatabaseUpgradeTest {

    @MockBean // S3Template을 MockBean으로 등록하여 실제 S3 연결 없이 테스트 가능하게 함
    private S3Template s3Template;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private UserActivityLogRepository userActivityLogRepository;

    // 사용자 CRUD 테스트
    @Test
    void testUserCrud() {
        // 생성
        User newUser = User.builder()
                .name("Test User")
                .email("test.user@example.com")
                .picture("http://example.com/pic.jpg")
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser.getId());

        // 조회
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("Test User", foundUser.get().getName());

        // 수정
        User userToUpdate = foundUser.get();
        userToUpdate.update("Updated Name", "http://example.com/newpic.jpg");
        userRepository.save(userToUpdate);
        User updatedUser = userRepository.findById(userToUpdate.getId()).get();
        assertEquals("Updated Name", updatedUser.getName());

        // 삭제
        userRepository.deleteById(updatedUser.getId());
        assertFalse(userRepository.findById(updatedUser.getId()).isPresent());
    }

    // 장소 CRUD 테스트
    @Test
    void testPlaceCrud() {
        // 장소 소유자 사용자 생성 (테스트용)
        User placeOwner = userRepository.findByEmail("place.owner@example.com")
                .orElseGet(() -> userRepository.save(User.builder().name("Place Owner").email("place.owner@example.com").role(Role.USER).build()));

        // 생성
        Place newPlace = Place.builder()
                .latitude(BigDecimal.valueOf(37.123456))
                .longitude(BigDecimal.valueOf(127.654321))
                .originalAddress("Test Address Original")
                .roadAddress("Test Address Road")
                .description("Test Place Description")
                .user(placeOwner)
                .build();
        Place savedPlace = placeRepository.save(newPlace);
        assertNotNull(savedPlace.getId());

        // 조회
        Optional<Place> foundPlace = placeRepository.findById(savedPlace.getId());
        assertTrue(foundPlace.isPresent());
        assertEquals("Test Place Description", foundPlace.get().getDescription());

        // 수정
        Place placeToUpdate = foundPlace.get();
        placeToUpdate.updateDescription("Updated Place Description");
        placeRepository.save(placeToUpdate);
        Place updatedPlace = placeRepository.findById(placeToUpdate.getId()).get();
        assertEquals("Updated Place Description", updatedPlace.getDescription());

        // 삭제
        placeRepository.deleteById(updatedPlace.getId());
        assertFalse(placeRepository.findById(updatedPlace.getId()).isPresent());
    }

    // 사용자 활동 로그 테스트
    @Test
    void testUserActivityLog() {
        // 활동 로그 사용자 생성 (테스트용)
        User logUser = userRepository.findByEmail("log.user@example.com")
                .orElseGet(() -> userRepository.save(User.builder().name("Log User").email("log.user@example.com").role(Role.USER).build()));

        // 생성
        UserActivityLog newLog = UserActivityLog.builder()
                .latitude(BigDecimal.valueOf(37.0))
                .longitude(BigDecimal.valueOf(127.0))
                .userId(logUser.getId())
                .sessionId("test-session-id-123")
                .build();
        UserActivityLog savedLog = userActivityLogRepository.save(newLog);
        assertNotNull(savedLog.getId());

        // 조회
        Optional<UserActivityLog> foundLog = userActivityLogRepository.findById(savedLog.getId());
        assertTrue(foundLog.isPresent());
        assertEquals("test-session-id-123", foundLog.get().getSessionId());

        // 정리: 로그 삭제
        userActivityLogRepository.deleteById(savedLog.getId());
        assertFalse(userActivityLogRepository.findById(savedLog.getId()).isPresent());
    }
}