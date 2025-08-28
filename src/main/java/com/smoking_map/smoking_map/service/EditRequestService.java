// src/main/java/com/smoking_map/smoking_map/service/EditRequestService.java
package com.smoking_map.smoking_map.service; // --- ▼▼▼ [수정] 패키지 경로 수정 ▼▼▼ ---

import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.domain.edit_request.EditRequest;
import com.smoking_map.smoking_map.domain.edit_request.EditRequestRepository;
import com.smoking_map.smoking_map.domain.edit_request.RequestStatus;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.EditRequestSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditRequestService {

    private final EditRequestRepository editRequestRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveEditRequest(Long placeId, EditRequestSaveDto requestDto, SessionUser sessionUser) {
        User user = userRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. email=" + sessionUser.getEmail()));

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        EditRequest editRequest = EditRequest.builder()
                .place(place)
                .user(user)
                .content(requestDto.getContent())
                .status(RequestStatus.PENDING)
                .build();

        editRequestRepository.save(editRequest);
    }
}