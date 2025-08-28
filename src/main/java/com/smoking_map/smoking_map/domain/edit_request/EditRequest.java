// src/main/java/com/smoking_map/smoking_map/domain/edit_request/EditRequest.java
package com.smoking_map.smoking_map.domain.edit_request;

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
public class EditRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Builder
    public EditRequest(Place place, User user, String content, RequestStatus status) {
        this.place = place;
        this.user = user;
        this.content = content;
        this.status = status;
    }

    public void updateStatus(RequestStatus status) {
        this.status = status;
    }
}