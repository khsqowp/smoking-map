package com.smoking_map.smoking_map.domain.favorite;

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
                @UniqueConstraint(columnNames = {"user_id", "place_id"})
        }
)
public class Favorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Builder
    public Favorite(User user, Place place) {
        this.user = user;
        this.place = place;
    }
}