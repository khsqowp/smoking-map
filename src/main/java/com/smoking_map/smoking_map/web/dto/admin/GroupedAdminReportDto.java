// GroupedAdminReportDto.java
package com.smoking_map.smoking_map.web.dto.admin;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class GroupedAdminReportDto {
    private final Long placeId;
    private final String roadAddress;
    private final Map<String, Long> reportTypeCounts; // 신고 유형별 횟수
    private final List<String> otherContents; // '기타' 유형의 내용 목록
}