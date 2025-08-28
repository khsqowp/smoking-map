package com.smoking_map.smoking_map.domain.review;

import com.smoking_map.smoking_map.domain.BaseTimeEntity;
import com.smoking_map.smoking_map.domain.place.Place;
import com.smoking_map.smoking_map.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "place_id"}) // 한 사용자는 한 장소에 하나의 리뷰만 작성 가능
        }
)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false)
    private int rating; // 1 ~ 5점

    @Column(length = 500)
    private String comment;

    @Builder
    public Review(User user, Place place, int rating, String comment) {
        this.user = user;
        this.place = place;
        this.rating = rating;
        this.comment = comment;
    }

    public void update(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}