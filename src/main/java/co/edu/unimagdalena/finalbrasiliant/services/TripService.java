package co.edu.unimagdalena.finalbrasiliant.services;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;

public interface TripService {
	TripResponse create(TripCreateRequest request);
	TripResponse get(Long id);
	TripResponse update(Long id, TripUpdateRequest request);
	TripResponse delete(Long id);
	
	Optional<TripResponse> getByRouteId(Long route_id);
    List<TripResponse> getAllByRouteId(Long route_id);
    Optional<TripResponse> getByBusId(Long bus_id);
    List<TripResponse> getByDepartureBetween(OffsetDateTime start, OffsetDateTime end);
    List<TripResponse> getByArrivalBetween(OffsetDateTime start, OffsetDateTime end);
    List<TripResponse> getByStatus(TripStatus status);
    List<TripResponse> getByRouteIdAndStatus(Long route_id, TripStatus status);
    List<TripResponse> getByDate(LocalDate date);
}
