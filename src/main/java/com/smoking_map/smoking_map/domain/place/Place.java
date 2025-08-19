package com.smoking_map.smoking_map.domain.place;

import com.smoking_map.smoking_map.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Place extends BaseTimeEntity { // BaseTimeEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... 기존 필드들 ...
    @Column(precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;

    @Column(length = 255)
    private String originalAddress;

    @Column(length = 255)
    private String roadAddress;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "place_image_urls", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();

    @Builder
    public Place(BigDecimal latitude, BigDecimal longitude, String originalAddress, String roadAddress, String description, List<String> imageUrls) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.originalAddress = originalAddress;
        this.roadAddress = roadAddress;
        this.description = description;
        if (imageUrls != null) {
            this.imageUrls = imageUrls;
        }
    }
}