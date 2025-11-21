package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatHoldDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;

import java.util.List;

public interface SeatHoldService {

    SeatHoldResponse create(Long tripId, SeatHoldCreateRequest request);
    SeatHoldResponse get(Long id);
    SeatHoldResponse update(Long id, SeatHoldUpdateRequest request);
    void delete(Long id);

    List<SeatHoldResponse> listByPassenger(Long passengerId);
    List<SeatHoldResponse> listByTrip(Long tripId);
    List<SeatHoldResponse> listByStatus(SeatHoldStatus status);
    List<SeatHoldResponse> listByTripAndPassenger(Long tripId, Long passengerId, SeatHoldStatus status);
    boolean existsActiveSeatHold(Long tripId, String seatNumber);
    List<SeatHoldResponse> listExpiredHolds();
}
