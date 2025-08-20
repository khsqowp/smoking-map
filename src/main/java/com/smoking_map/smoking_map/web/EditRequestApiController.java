// src/main/java/com/smoking_map/smoking_map/web/EditRequestApiController.java
package com.smoking_map.smoking_map.web;

import com.smoking_map.smoking_map.config.auth.LoginUser;
import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.service.EditRequestService;
import com.smoking_map.smoking_map.web.dto.EditRequestSaveDto;
import jakarta.validation.Valid; // --- ▼▼▼ [수정] import 추가 ▼▼▼ ---
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EditRequestApiController {

    private final EditRequestService editRequestService;

    @PostMapping("/api/v1/places/{placeId}/edit-requests")
    public ResponseEntity<Void> saveEditRequest(@PathVariable Long placeId,
                                                @Valid @RequestBody EditRequestSaveDto requestDto, // --- ▼▼▼ [수정] @Valid 추가 ▼▼▼ ---
                                                @LoginUser SessionUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        editRequestService.saveEditRequest(placeId, requestDto, user);
        return ResponseEntity.ok().build();
    }
}