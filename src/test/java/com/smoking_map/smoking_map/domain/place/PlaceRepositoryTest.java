package com.smoking_map.smoking_map.domain.place;

import com.smoking_map.smoking_map.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // JPA 관련 컴포넌트만 로드하여 테스트, 인메모리 DB(H2)를 사용
@DisplayName("PlaceRepository 통합 테스트")
class PlaceRepositoryTest {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private TestEntityManager entityManager; // 테스트용 엔티티를 관리 (persist, flush, find 등)

    private Place place1, place2, place3;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 미리 저장
        // 왜? 각 테스트는 독립적으로 실행되어야 하므로, 매번 동일한 초기 데이터 상태를 만들어주기 위함.
        User user = User.builder().email("test@test.com").name("tester").build();
        entityManager.persist(user);

        place1 = Place.builder()
                .latitude(BigDecimal.valueOf(37.123))
                .longitude(BigDecimal.valueOf(127.123))
                .roadAddress("서울 강남구 테헤란로")
                .originalAddress("서울 강남구 역삼동 123")
                .user(user)
                .build(); // 생성 시간은 자동 주입됨 (BaseTimeEntity)

        place2 = Place.builder()
                .latitude(BigDecimal.valueOf(37.456))
                .longitude(BigDecimal.valueOf(127.456))
                .roadAddress("서울 서초구 반포대로")
                .originalAddress("서울 서초구 반포동 456")
                .user(user)
                .build();

        place3 = Place.builder()
                .latitude(BigDecimal.valueOf(37.789))
                .longitude(BigDecimal.valueOf(127.789))
                .roadAddress("경기도 성남시 분당구")
                .originalAddress("경기도 성남시 분당동 789")
                .user(user)
                .build();

        entityManager.persist(place1);
        entityManager.persist(place2);
        // place3은 특정 테스트에서만 저장
    }

    @Nested
    @DisplayName("findByAddressKeyword (주소 키워드 검색) 테스트")
    class Describe_findByAddressKeyword {

        @Test
        @DisplayName("도로명 주소에 키워드가 포함된 장소를 모두 반환한다")
        void 성공_도로명_주소_검색() {
            // 준비 (Arrange)
            // 왜? 도로명 주소(roadAddress)를 기준으로 LIKE 검색이 정상 동작하는지 검증하기 위함.
            String keyword = "서울";

            // 실행 (Act)
            List<Place> result = placeRepository.findByAddressKeyword(keyword);

            // 검증 (Assert)
            assertThat(result).hasSize(2);
            assertThat(result).contains(place1, place2);
        }

        @Test
        @DisplayName("지번 주소에 키워드가 포함된 장소를 모두 반환한다")
        void 성공_지번_주소_검색() {
            // 준비 (Arrange)
            // 왜? 지번 주소(originalAddress)를 기준으로 LIKE 검색이 정상 동작하는지 검증하기 위함.
            String keyword = "역삼동";

            // 실행 (Act)
            List<Place> result = placeRepository.findByAddressKeyword(keyword);

            // 검증 (Assert)
            assertThat(result).hasSize(1);
            assertThat(result).contains(place1);
        }

        @Test
        @DisplayName("키워드에 해당하는 장소가 없으면, 빈 리스트를 반환한다")
        void 성공_검색_결과_없음() {
            // 준비 (Arrange)
            // 왜? 검색 결과가 없는 경우, null이 아닌 빈 리스트를 안전하게 반환하는지 검증하기 위함.
            String keyword = "없는주소";

            // 실행 (Act)
            List<Place> result = placeRepository.findByAddressKeyword(keyword);

            // 검증 (Assert)
            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("시간 기반 조회 테스트")
    class Describe_find_by_time {

        @Test
        @DisplayName("특정 시간 이후에 생성된 장소의 개수를 정확히 반환한다")
        void 성공_countByCreatedAtAfter() throws InterruptedException {
            // 준비 (Arrange)
            // 왜? 시간 비교(After)를 통한 COUNT 쿼리가 정확하게 동작하는지 검증하기 위함.
            LocalDateTime standardTime = LocalDateTime.now();
            Thread.sleep(10); // 시간차를 두기 위해 잠시 대기
            entityManager.persist(place3);
            entityManager.flush();

            // 실행 (Act)
            long count = placeRepository.countByCreatedAtAfter(standardTime);

            // 검증 (Assert)
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("특정 기간 사이에 생성된 장소의 개수를 정확히 반환한다")
        void 성공_countByCreatedAtBetween() throws InterruptedException {
            // 준비 (Arrange)
            // 왜? 시간 범위(Between)를 사용한 COUNT 쿼리가 정확하게 동작하는지 검증하기 위함.
            LocalDateTime startTime = LocalDateTime.now();
            Thread.sleep(10);
            entityManager.persist(place3);
            entityManager.flush();
            Thread.sleep(10);
            LocalDateTime endTime = LocalDateTime.now();

            // 실행 (Act)
            long count = placeRepository.countByCreatedAtBetween(startTime, endTime);

            // 검증 (Assert)
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("특정 시간 이전에 생성된 장소의 개수를 정확히 반환한다")
        void 성공_countByCreatedAtBefore() throws InterruptedException {
            // 준비 (Arrange)
            // 왜? 시간 비교(Before)를 통한 COUNT 쿼리가 정확하게 동작하는지 검증하기 위함.
            entityManager.persist(place3);
            entityManager.flush();
            Thread.sleep(10);
            LocalDateTime standardTime = LocalDateTime.now();

            // 실행 (Act)
            long count = placeRepository.countByCreatedAtBefore(standardTime);

            // 검증 (Assert)
            // place1, place2, place3 모두 standardTime 이전에 생성됨
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("특정 기간 사이에 생성된 장소의 목록을 정확히 반환한다")
        void 성공_findAllByCreatedAtBetween() throws InterruptedException {
            // 준비 (Arrange)
            // 왜? 시간 범위(Between)를 사용한 SELECT 쿼리가 정확하게 동작하는지 검증하기 위함.
            LocalDateTime startTime = LocalDateTime.now();
            Thread.sleep(10);
            entityManager.persist(place3);
            entityManager.flush();
            Thread.sleep(10);
            LocalDateTime endTime = LocalDateTime.now();

            // 실행 (Act)
            List<Place> result = placeRepository.findAllByCreatedAtBetween(startTime, endTime);

            // 검증 (Assert)
            assertThat(result).hasSize(1);
            assertThat(result).contains(place3);
        }
    }
}