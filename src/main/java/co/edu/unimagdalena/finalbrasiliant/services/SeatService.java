package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.*;

public interface SeatService {
	SeatResponse create(SeatCreateRequest request);
	SeatResponse get(Long id);
	SeatResponse update(Long id, SeatUpdateRequest request);
	SeatResponse delete(Long id);
}
