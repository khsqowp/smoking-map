package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.config.auth.LoginUser;
import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.service.FavoriteService;
import com.smoking_map.smoking_map.web.dto.PlaceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FavoriteApiController {

    private final FavoriteService favoriteService;

    @PostMapping("/api/v1/places/{placeId}/favorite")
    public ResponseEntity<Void> addFavorite(@PathVariable Long placeId, @LoginUser SessionUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        favoriteService.addFavorite(user.getEmail(), placeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/v1/places/{placeId}/favorite")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long placeId, @LoginUser SessionUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        favoriteService.removeFavorite(user.getEmail(), placeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/favorites")
    public ResponseEntity<List<PlaceResponseDto>> getFavorites(@LoginUser SessionUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(favoriteService.getFavorites(user.getEmail()));
    }
}