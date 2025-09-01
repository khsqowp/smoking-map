package com.smoking_map.smoking_map.domain.user;

import com.smoking_map.smoking_map.domain.place.Place;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1, user2, user3;

    @BeforeEach
    void setUp() {
        user1 = User.builder().email("user1@test.com").name("유저1").role(Role.USER).build();
        user2 = User.builder().email("user2@test.com").name("유저2").role(Role.USER).build();
        user3 = User.builder().email("user3@test.com").name("유저3").role(Role.USER).build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);

        // user1은 2개의 장소 등록
        entityManager.persist(Place.builder().user(user1).latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).build());
        entityManager.persist(Place.builder().user(user1).latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).build());

        // user2는 1개의 장소 등록
        entityManager.persist(Place.builder().user(user2).latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).build());

        // user3은 장소 등록 안함

        entityManager.flush();
    }

    @Nested
    @DisplayName("findByEmail (이메일로 사용자 조회) 테스트")
    class Describe_findByEmail {

        @Test
        @DisplayName("존재하는 이메일로 조회하면, 해당 사용자 정보를 담은 Optional 객체를 반환한다")
        void 성공_존재하는_이메일() {
            // 준비 (Arrange)
            // 왜? 사용자를 식별하는 가장 기본적인 쿼리가 정확하게 동작하는지 검증하기 위함.
            String existingEmail = "user1@test.com";

            // 실행 (Act)
            Optional<User> foundUser = userRepository.findByEmail(existingEmail);

            // 검증 (Assert)
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo(existingEmail);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회하면, 비어있는 Optional 객체를 반환한다")
        void 실패_존재하지_않는_이메일() {
            // 준비 (Arrange)
            // 왜? 조회 결과가 없는 경우, Null이 아닌 Optional.empty()를 안전하게 반환하는지 검증하기 위함.
            String nonExistingEmail = "nonexistent@test.com";

            // 실행 (Act)
            Optional<User> foundUser = userRepository.findByEmail(nonExistingEmail);

            // 검증 (Assert)
            assertThat(foundUser).isNotPresent();
        }
    }

    @Nested
    @DisplayName("countPlacesByUser (사용자별 장소 등록 수 집계) 테스트")
    class Describe_countPlacesByUser {

        @Test
        @DisplayName("각 사용자가 등록한 장소의 수를 정확하게 집계하여 반환한다")
        void 성공_사용자별_장소_수_집계() {
            // 준비 (Arrange)
            // 왜? GROUP BY와 COUNT를 사용하는 복잡한 JPQL 쿼리가 올바른 집계 결과를 반환하는지 검증하기 위함.

            // 실행 (Act)
            List<Object[]> results = userRepository.countPlacesByUser();

            // 검증 (Assert)
            // 장소를 등록한 user1, user2만 결과에 포함되어야 함
            assertThat(results).hasSize(2);

            // user1의 결과 검증
            Optional<Object[]> user1Result = results.stream()
                    .filter(result -> ((User) result[0]).getId().equals(user1.getId()))
                    .findFirst();
            assertThat(user1Result).isPresent();
            assertThat(user1Result.get()[1]).isEqualTo(2L); // user1은 2개 등록

            // user2의 결과 검증
            Optional<Object[]> user2Result = results.stream()
                    .filter(result -> ((User) result[0]).getId().equals(user2.getId()))
                    .findFirst();
            assertThat(user2Result).isPresent();
            assertThat(user2Result.get()[1]).isEqualTo(1L); // user2는 1개 등록
        }
    }
}