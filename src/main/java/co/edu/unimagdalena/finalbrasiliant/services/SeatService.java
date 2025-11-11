package co.edu.unimagdalena.finalbrasiliant.services;

import java.util.List;
import java.util.Optional;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;

public interface SeatService {
	SeatResponse create(SeatCreateRequest request);
	SeatResponse get(Long id);
	SeatResponse update(Long id, SeatUpdateRequest request);
	void delete(Long id);
	
	List<SeatResponse> getSeatsByBus(Long bus_id);
    SeatResponse getSeatByNumberAndBus(String number, Long bus_id);
    List<SeatResponse> getSeatsByType(SeatType type);
    SeatResponse getSeatByNumberAndTrip(String number, Long trip_id);
}
