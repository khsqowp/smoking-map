package com.smoking_map.smoking_map.domain.place;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    long countByCreatedAtAfter(LocalDateTime startOfDay);
    List<Place> findTop5ByOrderByIdDesc();

    // [추가] 도로명 주소로 장소를 검색하는 메서드 (Containing -> LIKE '%...%')
    List<Place> findByRoadAddressContaining(String roadAddress);
}