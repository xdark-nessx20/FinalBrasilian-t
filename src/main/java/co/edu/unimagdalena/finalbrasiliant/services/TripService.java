package co.edu.unimagdalena.finalbrasiliant.services;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;

public interface TripService {
	TripResponse create(TripCreateRequest request);
	TripResponse get(Long id);
	TripResponse update(Long id, TripUpdateRequest request);
	void delete(Long id);
	
	Page<TripResponse> getAllByRouteId(Long route_id, Pageable page);
    Page<TripResponse> getAllByBusId(Long bus_id, Pageable page);
    Page<TripResponse> getByDepartureBetween(OffsetDateTime start, OffsetDateTime end, Pageable page);
    Page<TripResponse> getByArrivalBetween(OffsetDateTime start, OffsetDateTime end, Pageable page);
    Page<TripResponse> getByStatus(TripStatus status, Pageable page);
    List<TripResponse> getByRouteIdAndStatus(Long route_id, TripStatus status);
    Page<TripResponse> getByDate(LocalDate date, Pageable page);
}
