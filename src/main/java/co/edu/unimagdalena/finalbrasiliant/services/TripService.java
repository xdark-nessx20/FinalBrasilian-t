package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.*;

public interface TripService {
	TripResponse create(TripCreateRequest request);
	TripResponse get(Long id);
	TripResponse update(Long id, TripUpdateRequest request);
	TripResponse delete(Long id);
}
