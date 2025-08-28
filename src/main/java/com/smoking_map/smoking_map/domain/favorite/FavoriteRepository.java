package com.smoking_map.smoking_map.domain.favorite;

import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndPlace(User user, Place place);

    List<Favorite> findByUser(User user);

    boolean existsByUserAndPlace(User user, Place place);

    @Query("SELECT f.place.id FROM Favorite f WHERE f.user = :user")
    Set<Long> findPlaceIdsByUser(@Param("user") User user);

    // --- ▼▼▼ [추가] 장소별 즐겨찾기 수를 계산하는 쿼리 ▼▼▼ ---
    @Query("SELECT f.place.id, COUNT(f) FROM Favorite f GROUP BY f.place.id")
    List<Object[]> countFavoritesByPlace();
}