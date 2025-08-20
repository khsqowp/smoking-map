package com.smoking_map.smoking_map.service;

import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.report.Report;
import com.smoking_map.smoking_map.domain.report.ReportRepository;
import com.smoking_map.smoking_map.domain.user.Role;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.web.dto.ReportRequestDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Transactional
    public void submitReport(Long placeId, ReportRequestDto requestDto) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + placeId));

        User user = null;
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        if (sessionUser != null) {
            user = userRepository.findByEmail(sessionUser.getEmail()).orElse(null);

            if (user != null && user.getRole() == Role.USER) {
                boolean alreadyReported = reportRepository.existsByUserIdAndPlaceId(user.getId(), placeId);
                if (alreadyReported) {
                    throw new IllegalArgumentException("이미 이 장소에 대해 신고한 내역이 있습니다.");
                }
            }
        }


        Report report = Report.builder()
                .place(place)
                .user(user)
                .type(requestDto.getType())
                .content(requestDto.getContent())
                .build();

        reportRepository.save(report);
    }
}