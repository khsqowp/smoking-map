package com.smoking_map.smoking_map.domain.place;

import com.smoking_map.smoking_map.domain.BaseTimeEntity;
import com.smoking_map.smoking_map.domain.user.User;
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
public class Place extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Place(BigDecimal latitude, BigDecimal longitude, String originalAddress, String roadAddress, String description, List<String> imageUrls, User user) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.originalAddress = originalAddress;
        this.roadAddress = roadAddress;
        this.description = description;
        this.user = user;
        if (imageUrls != null) {
            this.imageUrls = imageUrls;
        }
    }

    // 장소 수정 기능을 위한 메서드
    public void updateDescription(String description) {
        this.description = description;
    }
}