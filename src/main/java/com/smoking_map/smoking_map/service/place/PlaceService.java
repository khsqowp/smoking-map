package com.smoking_map.smoking_map.service.place;

import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.service.geocoding.GeocodingService;
import com.smoking_map.smoking_map.service.s3.FileValidator; // FileValidator import 추가
import com.smoking_map.smoking_map.service.s3.S3Uploader;
import com.smoking_map.smoking_map.web.dto.PlaceResponseDto;
import com.smoking_map.smoking_map.web.dto.PlaceSaveRequestDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    private final GeocodingService geocodingService;
    private final HttpSession httpSession;
    private final FileValidator fileValidator; // FileValidator 의존성 주입

    @Transactional
    public Long save(PlaceSaveRequestDto requestDto, List<MultipartFile> images) throws IOException {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        User user = userRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. email=" + sessionUser.getEmail()));

        GeocodingService.GeocodingResult geocodingResult = geocodingService.getAddressFromCoords(
                requestDto.getLatitude().doubleValue(),
                requestDto.getLongitude().doubleValue()
        );

        Place finalPlace = Place.builder()
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .originalAddress(requestDto.getOriginalAddress())
                .description(requestDto.getDescription())
                .roadAddress(geocodingResult.getFullAddress())
                .user(user)
                .build();

        if (images != null && !images.isEmpty()) {
            // --- ▼▼▼ [수정] 파일 검증 로직 추가 ▼▼▼ ---
            for (MultipartFile image : images) {
                fileValidator.validateImageFile(image);
            }
            // --- ▲▲▲ [수정] 파일 검증 로직 추가 ▲▲▲ ---

            String dirPath = "places/" + geocodingResult.getSido();
            int sequence = 1;
            for (MultipartFile image : images) {
                String filename = createS3FileName(image.getOriginalFilename(), sequence++, geocodingResult.getSigungu());
                String fullPath = dirPath + "/" + filename;
                String imageUrl = s3Uploader.upload(image, fullPath);
                finalPlace.getImageUrls().add(imageUrl);
            }
        }
        return placeRepository.save(finalPlace).getId();
    }

    @Transactional
    public List<String> addImages(Long id, List<MultipartFile> images) throws IOException {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + id));

        GeocodingService.GeocodingResult geocodingResult = geocodingService.getAddressFromCoords(
                place.getLatitude().doubleValue(),
                place.getLongitude().doubleValue()
        );

        List<String> newImageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            // --- ▼▼▼ [수정] 파일 검증 로직 추가 ▼▼▼ ---
            for (MultipartFile image : images) {
                fileValidator.validateImageFile(image);
            }
            // --- ▲▲▲ [수정] 파일 검증 로직 추가 ▲▲▲ ---

            String dirPath = "places/" + geocodingResult.getSido();
            int sequence = place.getImageUrls().size() + 1;
            for (MultipartFile image : images) {
                String filename = createS3FileName(image.getOriginalFilename(), sequence++, geocodingResult.getSigungu());
                String fullPath = dirPath + "/" + filename;
                String imageUrl = s3Uploader.upload(image, fullPath);
                newImageUrls.add(imageUrl);
            }
            place.getImageUrls().addAll(newImageUrls);
        }
        return newImageUrls;
    }

    private String createS3FileName(String originalFileName, int sequence, String sigungu) {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS"));
        String randomString = RandomStringUtils.randomAlphanumeric(5);
        String extension = StringUtils.getFilenameExtension(originalFileName);
        String sanitizedSigungu = sigungu.replaceAll("\\s+", "_");
        return String.format("%s_%s_%s_%d.%s", dateTime, sanitizedSigungu, randomString, sequence, extension);
    }

    @Transactional(readOnly = true)
    public PlaceResponseDto findById(Long id) {
        Place entity = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + id));
        return new PlaceResponseDto(entity);
    }

    @Transactional(readOnly = true)
    public List<PlaceResponseDto> findAll() {
        return placeRepository.findAll().stream()
                .map(PlaceResponseDto::new)
                .collect(Collectors.toList());
    }

    //    조회수 증가 서비스 로직
    @Transactional
    public void increaseViewCount(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + id));
        place.increaseViewCount();
    }
}