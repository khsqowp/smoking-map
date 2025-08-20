// src/main/java/com/smoking_map/smoking_map/web/dto/EditRequestSaveDto.java
package com.smoking_map.smoking_map.web.dto;

import jakarta.validation.constraints.NotBlank; // --- ▼▼▼ [수정] import 추가 ▼▼▼ ---
import jakarta.validation.constraints.Size;   // --- ▼▼▼ [수정] import 추가 ▼▼▼ ---
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EditRequestSaveDto {

    @NotBlank(message = "수정 제안 내용은 비워둘 수 없습니다.") // --- ▼▼▼ [수정] 어노테이션 추가 ▼▼▼ ---
    @Size(max = 1000, message = "내용은 1000자를 초과할 수 없습니다.") // --- ▼▼▼ [수정] 어노테이션 추가 ▼▼▼ ---
    private String content;
}