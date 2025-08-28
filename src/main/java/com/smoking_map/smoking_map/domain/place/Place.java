package com.smoking_map.smoking_map.domain.place;

import com.smoking_map.smoking_map.domain.BaseTimeEntity;
import com.smoking_map.smoking_map.domain.user.User;
import com.smoking_map.smoking_map.domain.edit_request.EditRequest; // --- ▼▼▼ [추가] import ▼▼▼ ---
import com.smoking_map.smoking_map.domain.review.Review; // --- ▼▼▼ [추가] import ▼▼▼ ---
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Column(columnDefinition = "integer default 0")
    private int viewCount = 0;

    // --- ▼▼▼ [추가] 평균 평점 및 리뷰 수 필드 ▼▼▼ ---
    @Column(columnDefinition = "double default 0.0")
    private double averageRating = 0.0;

    @Column(columnDefinition = "integer default 0")
    private int reviewCount = 0;
    // --- ▲▲▲ [추가] 평균 평점 및 리뷰 수 필드 ▲▲▲ ---


    // --- ▼▼▼ [수정] 정렬 순서 변경: 대표 이미지가 맨 앞으로, 나머지는 ID 순으로 ▼▼▼ ---
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("isRepresentative DESC, id ASC")
    private List<ImageInfo> imageInfos = new ArrayList<>();
    // --- ▲▲▲ [수정] 정렬 순서 변경 ▲▲▲ ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // --- ▼▼▼ [추가] 리뷰 목록과의 양방향 연관관계 ▼▼▼ ---
    @OneToMany(mappedBy = "place", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "place", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<EditRequest> editRequests = new ArrayList<>();

    @Builder
    public Place(BigDecimal latitude, BigDecimal longitude, String originalAddress, String roadAddress, String description, User user) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.originalAddress = originalAddress;
        this.roadAddress = roadAddress;
        this.description = description;
        this.user = user;
    }

    public void addImageInfo(ImageInfo imageInfo) {
        this.imageInfos.add(imageInfo);
        imageInfo.setPlace(this);
    }

    public List<String> getImageUrls() {
        return this.imageInfos.stream()
                .map(ImageInfo::getImageUrl)
                .collect(Collectors.toList());
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
    // --- ▼▼▼ [추가] 평점 및 리뷰 수 업데이트 메서드 ▼▼▼ ---
    public void updateReviewStats(double averageRating, int reviewCount) {
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }
}