package com.smoking_map.smoking_map.domain.place;

import com.smoking_map.smoking_map.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ImageInfo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    private Double gpsLatitude;

    private Double gpsLongitude;

    private String cameraModel;

    private LocalDateTime dateTimeOriginal;

    // --- ▼▼▼ [수정] 대표 이미지 여부를 나타내는 필드 추가 ▼▼▼ ---
    @Column(nullable = false)
    private boolean isRepresentative = false;
    // --- ▲▲▲ [수정] 대표 이미지 여부를 나타내는 필드 추가 ▲▲▲ ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Builder
    public ImageInfo(String imageUrl, Double gpsLatitude, Double gpsLongitude, String cameraModel, LocalDateTime dateTimeOriginal, Place place) {
        this.imageUrl = imageUrl;
        this.gpsLatitude = gpsLatitude;
        this.gpsLongitude = gpsLongitude;
        this.cameraModel = cameraModel;
        this.dateTimeOriginal = dateTimeOriginal;
        this.place = place;
    }
}