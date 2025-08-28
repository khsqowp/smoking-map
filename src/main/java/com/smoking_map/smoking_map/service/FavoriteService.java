package com.smoking_map.smoking_map.service;

import com.smoking_map.smoking_map.domain.favorite.Favorite;
import com.smoking_map.smoking_map.domain.favorite.FavoriteRepository;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.PlaceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public void addFavorite(String userEmail, Long placeId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found"));

        if (favoriteRepository.existsByUserAndPlace(user, place)) {
            throw new IllegalStateException("Already favorited");
        }

        favoriteRepository.save(Favorite.builder().user(user).place(place).build());
    }

    @Transactional
    public void removeFavorite(String userEmail, Long placeId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found"));

        Favorite favorite = favoriteRepository.findByUserAndPlace(user, place)
                .orElseThrow(() -> new IllegalArgumentException("Favorite not found"));

        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public List<PlaceResponseDto> getFavorites(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 즐겨찾기한 장소는 모두 isFavorited가 true여야 함
        Set<Long> favoritedPlaceIds = Collections.singleton(0L); // 임시값, 실제로는 필요 없음

        return favoriteRepository.findByUser(user).stream()
                .map(favorite -> new PlaceResponseDto(favorite.getPlace(), true))
                .collect(Collectors.toList());
    }
}