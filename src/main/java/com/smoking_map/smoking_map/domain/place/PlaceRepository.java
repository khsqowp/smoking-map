package com.smoking_map.smoking_map.domain.place;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    long countByCreatedAtAfter(LocalDateTime startOfDay);
    List<Place> findTop5ByOrderByIdDesc();
}