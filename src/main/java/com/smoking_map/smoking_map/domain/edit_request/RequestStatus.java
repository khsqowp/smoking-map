// src/main/java/com/smoking_map/smoking_map/domain/edit_request/RequestStatus.java
package com.smoking_map.smoking_map.domain.edit_request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestStatus {
    PENDING("대기중"),
    REVIEWED("검토 완료");

    private final String description;
}