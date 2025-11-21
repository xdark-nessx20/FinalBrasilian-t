package co.edu.unimagdalena.finalbrasiliant.services.impl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import co.edu.unimagdalena.finalbrasiliant.domain.repositories.AssignmentRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.ItCantBeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.TripService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.TripMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {
	private final TripRepository tripRepo;
	private final TripMapper tripMapper;
	private final RouteRepository routeRepo;
	private final BusRepository busRepo;
    private final AssignmentRepository assignRepo;
	
	@Transactional
	@Override
	public TripResponse create(Long routeId, TripCreateRequest request) {
		var bus = busRepo.findById(request.bus_id())
				.orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(request.bus_id())));
		var route = routeRepo.findById(routeId)
			    .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(routeId)));
		Trip trip = tripMapper.toEntity(request);
		trip.setBus(bus);
		trip.setRoute(route);
	    Trip saved = tripRepo.save(trip);
	    return tripMapper.toResponse(saved);
	}
	
	@Override
    public TripResponse get(Long id) {
        return tripRepo.findById(id).map(tripMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("trip %d not found".formatted(id)));
    }
	
	@Override
    @Transactional
    public TripResponse update(Long id, TripUpdateRequest request) {
        var trip = tripRepo.findById(id).orElseThrow(() -> new NotFoundException("trip %d not found.".formatted(id)));
        if (request.bus_id() != null) {
            var bus = busRepo.findById(request.bus_id())
                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(request.bus_id())));
            trip.setBus(bus);
        }
        tripMapper.patch(trip, request);
        return tripMapper.toResponse(tripRepo.save(trip));
    }
	
	@Override
    @Transactional
    public void delete(Long id) {
        tripRepo.deleteById(id);
    }
	
	
	@Override
	public Page<TripResponse> getAllByRouteId(Long route_id, Pageable page){
		Page<Trip> trips = tripRepo.findAllByRoute_Id(route_id, page);
		if (trips.isEmpty()) {
		    throw new NotFoundException("No trips found with route %s".formatted(route_id));
		}
		return trips.map(tripMapper::toResponse);
	}
		
	@Override
	public Page<TripResponse> getAllByBusId(Long bus_id, Pageable page){
		Page<Trip> trips = tripRepo.findAllByBus_Id(bus_id, page);
		if (trips.isEmpty()) {
		    throw new NotFoundException("No trips found with bus %s".formatted(bus_id));
		}
		return trips.map(tripMapper::toResponse);
	}
	
	@Override
	public Page<TripResponse> getByDepartureBetween(OffsetDateTime start, OffsetDateTime end, Pageable page){
		Page<Trip> trips = tripRepo.findAllByDepartureAtBetween(start, end, page);
		if(trips.isEmpty()) {
			throw new NotFoundException("No trips found between %s and %s".formatted(start, end));
		}
		return trips.map(tripMapper::toResponse);
	}
	
	@Override
	public Page<TripResponse> getByArrivalBetween(OffsetDateTime start, OffsetDateTime end, Pageable page){
		Page<Trip> trips = tripRepo.findAllByArrivalETABetween(start, end, page);
		if(trips.isEmpty()) {
			throw new NotFoundException("No trips found between %s and %d".formatted(start, end));
		}
		return trips.map(tripMapper::toResponse);
	}
	
	@Override
	public Page<TripResponse> getByStatus(TripStatus status, Pageable page){
		Page<Trip> trips = tripRepo.findAllByStatus(status, page);
		if(trips.isEmpty()) {
			throw new NotFoundException("No trips found with status %s".formatted(status));
		}
		return trips.map(tripMapper::toResponse);
	}
	
	@Override
	public List<TripResponse> getByRouteIdAndStatus(Long route_id, TripStatus status){
		List<Trip> trips = tripRepo.findAllByRoute_IdAndStatus(route_id, status);
		if(trips.isEmpty()) {
			throw new NotFoundException("No trips found with status %s and route %d".formatted(status, route_id));
		}
		return trips.stream().map(tripMapper::toResponse).toList();
	}
	
	@Override
	public Page<TripResponse> getByDate(LocalDate date, Pageable page){
		Page<Trip> trips = tripRepo.findAllByDate(date, page);
		if(trips.isEmpty()) {
			throw new NotFoundException("No trips found in the date %s".formatted(date));
		}
		return trips.map(tripMapper::toResponse);
	}
	
	@Override
	public List<TripResponse> getByRouteIdAndDate(Long route_id, LocalDate date){
		List<Trip> trips = tripRepo.findAllByRoute_IdAndDate(route_id, date);
		if(trips.isEmpty()) {
			throw new NotFoundException("No trips found in route %s and date %s".formatted(route_id, date));
		}
		return trips.stream().map(tripMapper::toResponse).toList();
	}

    @Override
    @Transactional
    public TripResponse openBoarding(Long id) {
        var trip = tripRepo.findById(id).orElseThrow(() -> new NotFoundException("Trip %d not found.".formatted(id)));
        if (!trip.getStatus().equals(TripStatus.SCHEDULED))
            throw new ItCantBeException("Just is allowed to open SCHEDULED trips");
        trip.setStatus(TripStatus.BOARDING);
        return tripMapper.toResponse(tripRepo.save(trip));
    }

    @Override
    public TripResponse closeBoarding(Long id) {
        var trip = tripRepo.findById(id).orElseThrow(() -> new NotFoundException("Trip %d not found.".formatted(id)));
        if (!trip.getStatus().equals(TripStatus.BOARDING))
            throw new ItCantBeException("Just is allowed to close BOARDING trips");
        trip.setStatus(TripStatus.BOARDING_CLOSED);
        return tripMapper.toResponse(tripRepo.save(trip));
    }

    @Override
    public TripResponse depart(Long id) {
        var trip = tripRepo.findById(id).orElseThrow(() -> new NotFoundException("Trip %d not found.".formatted(id)));
        if (!trip.getStatus().equals(TripStatus.BOARDING_CLOSED))
            throw new ItCantBeException("Just is allowed to open BOARDING_CLOSED trips");
        trip.setStatus(TripStatus.DEPARTED);
        return tripMapper.toResponse(tripRepo.save(trip));
    }
}
