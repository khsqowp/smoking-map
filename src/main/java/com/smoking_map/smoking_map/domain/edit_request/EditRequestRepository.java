// src/main/java/com/smoking_map/smoking_map/domain/edit_request/EditRequestRepository.java
package com.smoking_map.smoking_map.domain.edit_request;

import com.smoking_map.smoking_map.domain.place.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EditRequestRepository extends JpaRepository<EditRequest, Long> {

    List<EditRequest> findAllByPlaceAndStatus(Place place, RequestStatus status);

    List<EditRequest> findAllByStatus(RequestStatus status);
}