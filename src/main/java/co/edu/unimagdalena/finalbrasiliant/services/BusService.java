package co.edu.unimagdalena.finalbrasiliant.services;


import co.edu.unimagdalena.finalbrasiliant.api.dto.BusDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;

import java.util.List;
import java.util.Set;

public interface BusService {

    BusResponse create(BusCreateRequest request);
    BusResponse get(Long id);
    BusResponse update(Long id, BusUpdateRequest request);
    void delete(Long id);

    BusResponse getByPlate(String plate);
    List<BusResponse> listByStatus(BusStatus status);
    List<BusResponse> listByCapacityGreaterThanEqual(Integer capacity);
    List<BusResponse> listByAmenities(Set<String> amenities);

}
