package com.smoking_map.smoking_map.service.place;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.smoking_map.smoking_map.config.auth.dto.SessionUser;
import com.smoking_map.smoking_map.domain.place.ImageInfo;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.place.PlaceRepository;
import com.smoking_map.smoking_map.domain.favorite.FavoriteRepository;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.user.UserRepository;
import com.smoking_map.smoking_map.service.geocoding.GeocodingService;
import com.smoking_map.smoking_map.service.s3.FileValidator;
import com.smoking_map.smoking_map.service.s3.S3Uploader;
import com.smoking_map.smoking_map.web.dto.PlaceResponseDto;
import com.smoking_map.smoking_map.web.dto.PlaceSaveRequestDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    private final GeocodingService geocodingService;
    private final HttpSession httpSession;
    private final FileValidator fileValidator;
    private final FavoriteRepository favoriteRepository;


    @CacheEvict(value = {"allPlaces", "searchResults"}, allEntries = true)
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
            String dirPath = "places/" + geocodingResult.getSido();
            int sequence = 1;

            for (MultipartFile image : images) {
                fileValidator.validateImageFile(image);

                byte[] imageBytes = image.getBytes();

                InputStream metadataInputStream = new ByteArrayInputStream(imageBytes);
                ImageInfo imageInfo = extractMetadataFromImage(metadataInputStream, image.getOriginalFilename());

                InputStream s3InputStream = new ByteArrayInputStream(imageBytes);
                String filename = createS3FileName(image.getOriginalFilename(), sequence++, geocodingResult.getSigungu());
                String fullPath = dirPath + "/" + filename;
                String imageUrl = s3Uploader.upload(s3InputStream, fullPath, image.getOriginalFilename());
                imageInfo.setImageUrl(imageUrl);

                finalPlace.addImageInfo(imageInfo);
            }
        }
        return placeRepository.save(finalPlace).getId();
    }

    private ImageInfo extractMetadataFromImage(InputStream inputStream, String originalFilename) {
        ImageInfo.ImageInfoBuilder builder = ImageInfo.builder();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);

            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                builder.gpsLatitude(gpsDirectory.getGeoLocation().getLatitude());
                builder.gpsLongitude(gpsDirectory.getGeoLocation().getLongitude());
            }

            ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDirectory != null) {
                builder.cameraModel(exifDirectory.getString(ExifSubIFDDirectory.TAG_LENS_MODEL));
                Date originalDate = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (originalDate != null) {
                    builder.dateTimeOriginal(originalDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
            }
        } catch (ImageProcessingException | IOException e) {
            log.warn("이미지 메타데이터를 읽는 중 오류가 발생했습니다: {}", originalFilename, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("InputStream을 닫는 중 오류 발생", e);
            }
        }
        return builder.build();
    }

    @CacheEvict(value = {"places", "allPlaces"}, allEntries = true)
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
            String dirPath = "places/" + geocodingResult.getSido();
            int sequence = place.getImageInfos().size() + 1;
            for (MultipartFile image : images) {
                fileValidator.validateImageFile(image);

                byte[] imageBytes = image.getBytes();

                InputStream metadataInputStream = new ByteArrayInputStream(imageBytes);
                ImageInfo imageInfo = extractMetadataFromImage(metadataInputStream, image.getOriginalFilename());

                InputStream s3InputStream = new ByteArrayInputStream(imageBytes);
                String filename = createS3FileName(image.getOriginalFilename(), sequence++, geocodingResult.getSigungu());
                String fullPath = dirPath + "/" + filename;
                String imageUrl = s3Uploader.upload(s3InputStream, fullPath, image.getOriginalFilename());

                imageInfo.setImageUrl(imageUrl);
                place.addImageInfo(imageInfo);
                newImageUrls.add(imageUrl);
            }
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

    @Cacheable(value = "places", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public PlaceResponseDto findById(Long id) {
        Place entity = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + id));

        // --- ▼▼▼ [수정] 로그인 사용자 즐겨찾기 여부 확인 로직 추가 ▼▼▼ ---
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        if (sessionUser != null) {
            User user = userRepository.findByEmail(sessionUser.getEmail()).orElse(null);
            if (user != null) {
                boolean isFavorited = favoriteRepository.existsByUserAndPlace(user, entity);
                return new PlaceResponseDto(entity, isFavorited);
            }
        }
        return new PlaceResponseDto(entity, false);
    }

    @Cacheable(value = "allPlaces", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<PlaceResponseDto> findAll() {
        // --- ▼▼▼ [수정] 로그인 사용자 즐겨찾기 여부 확인 로직 추가 ▼▼▼ ---
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        Set<Long> favoritedPlaceIds = Collections.emptySet();

        if (sessionUser != null) {
            User user = userRepository.findByEmail(sessionUser.getEmail()).orElse(null);
            if (user != null) {
                favoritedPlaceIds = favoriteRepository.findPlaceIdsByUser(user);
            }
        }

        final Set<Long> finalFavoritedPlaceIds = favoritedPlaceIds;
        return placeRepository.findAll().stream()
                .map(place -> new PlaceResponseDto(place, finalFavoritedPlaceIds.contains(place.getId())))
                .collect(Collectors.toList());
    }


    @Transactional
    public void increaseViewCount(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 없습니다. id=" + id));
        place.increaseViewCount();
    }

    // --- ▼▼▼ [추가] 주소 검색 서비스 메서드 ▼▼▼ ---
    @Cacheable(value = "searchResults", key = "#keyword", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<PlaceResponseDto> searchPlacesByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        return placeRepository.findByAddressKeyword(keyword).stream()
                .map(PlaceResponseDto::new)
                .collect(Collectors.toList());
    }


}