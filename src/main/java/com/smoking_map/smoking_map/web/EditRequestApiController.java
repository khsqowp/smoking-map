// src/main/java/com/smoking_map/smoking_map/web/EditRequestApiController.java
package com.smoking_map.smoking_map.web;

// --- ▼▼▼ [수정] 누락된 import 구문 전체 추가 ▼▼▼ ---
import com.smoking_map.smoking_map.config.auth.LoginUser;
import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.service.EditRequestService;
import com.smoking_map.smoking_map.web.dto.EditRequestSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// --- ▲▲▲ [수정] 누락된 import 구문 전체 추가 ▲▲▲ ---

@RestController
@RequiredArgsConstructor
public class EditRequestApiController {

    private final EditRequestService editRequestService;

    @PostMapping("/api/v1/places/{placeId}/edit-requests")
    public ResponseEntity<Void> saveEditRequest(@PathVariable Long placeId,
                                                @RequestBody EditRequestSaveDto requestDto,
                                                @LoginUser SessionUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        editRequestService.saveEditRequest(placeId, requestDto, user);
        return ResponseEntity.ok().build();
    }
}