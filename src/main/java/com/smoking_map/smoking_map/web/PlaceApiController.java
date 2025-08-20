package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.domain.user.Role;
import com.smoking_map.smoking_map.service.place.PlaceService;
import com.smoking_map.smoking_map.web.dto.PlaceResponseDto;
import com.smoking_map.smoking_map.web.dto.PlaceSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PlaceApiController {

    private final PlaceService placeService;

    @PostMapping(value = "/api/v1/places", consumes = "multipart/form-data")
    public ResponseEntity<Long> save(@RequestPart("requestDto") PlaceSaveRequestDto requestDto,
                                     @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        return ResponseEntity.ok(placeService.save(requestDto, images));
    }

    // --- [새로 추가된 메서드] ---
    // 특정 장소에 이미지를 추가하는 API
    @PostMapping("/api/v1/places/{id}/images")
    public ResponseEntity<List<String>> addImages(@PathVariable Long id,
                                                  @RequestPart(value = "images") List<MultipartFile> images) throws IOException {
        List<String> imageUrls = placeService.addImages(id, images);
        return ResponseEntity.ok(imageUrls);
    }
    // --- [여기까지] ---

    @GetMapping("/api/v1/places")
    public ResponseEntity<List<PlaceResponseDto>> findAll() {
        return ResponseEntity.ok(placeService.findAll());
    }

    @GetMapping("/api/v1/places/{id}")
    public ResponseEntity<PlaceResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(placeService.findById(id));
    }

    //조회수 증가 API
    @PostMapping("/api/v1/places/{id}/view")
    public ResponseEntity<Void> increaseViewCount(@PathVariable Long id) {
        placeService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }
}