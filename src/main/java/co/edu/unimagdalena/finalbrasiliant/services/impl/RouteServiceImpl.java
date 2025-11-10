package co.edu.unimagdalena.finalbrasiliant.services.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.RouteCreateRequest;
import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.RouteResponse;
import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.RouteUpdateRequest;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.RouteService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.RouteMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {
	private final RouteRepository routeRepo;
	private final RouteMapper routeMapper;
	
	@Transactional
	@Override
	public RouteResponse create(RouteCreateRequest request) {
	    Route route = routeMapper.toEntity(request);
	    Route saved = routeRepo.save(route);
	    return routeMapper.toResponse(saved);
	}
	
	@Override
    public RouteResponse get(Long id) {
        return routeRepo.findById(id).map(routeMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("route %d not found".formatted(id)));
    }
	
	@Override
    @Transactional
    public RouteResponse update(Long id, RouteUpdateRequest request) {
        var route = routeRepo.findById(id).orElseThrow(() -> new NotFoundException("route %d not found.".formatted(id)));
        routeMapper.patch(route, request);
        return routeMapper.toResponse(routeRepo.save(route));
    }
	
	@Override
    @Transactional
    public void delete(Long id) {
        routeRepo.deleteById(id);
    }
	
	@Override
	public RouteResponse getByCode(String code) {
		return routeRepo.findByCode(code)
				.map(routeMapper::toResponse).orElseThrow(()-> new NotFoundException("route with code %s not found".formatted(code)));
	}
	
	@Override
	public RouteResponse getByRouteName(String routeName) {
		return routeRepo.findByRouteName(routeName)
				.map(routeMapper::toResponse).orElseThrow(()-> new NotFoundException("routed called %s not found".formatted(routeName)));
	}
	
	@Override
	public List<RouteResponse> getByOrigin(String origin){
		List<Route> routes = routeRepo.findAllByOriginIgnoreCase(origin);
		if(routes.isEmpty()) {
			throw new NotFoundException("No routes founds with origin %s".formatted(origin));
		}
		return routes.stream().map(routeMapper::toResponse).toList();
	}
	
	@Override
	public List<RouteResponse> getByDestination(String destination){
		List<Route> routes = routeRepo.findAllByDestinationIgnoreCase(destination);
		if(routes.isEmpty()) {
			throw new NotFoundException("No routes founds with destination %s".formatted(destination));
		}
		return routes.stream().map(routeMapper::toResponse).toList();
	}
	
	@Override
	public List<RouteResponse> getByMinDurationGreaterThan(Integer minDuration){
		List<Route> routes = routeRepo.findAllByDurationMinGreaterThan(minDuration);
		if(routes.isEmpty()) {
			throw new NotFoundException("No routes founds with a duration less than %s".formatted(minDuration));
		}
		return routes.stream().map(routeMapper::toResponse).toList();
	}
	
	@Override
	public List<RouteResponse> getByDurationBetween(Integer minDuration, Integer maxDuration){
		List<Route> routes = routeRepo.findAllByDurationMinBetween(minDuration, maxDuration);
		if(routes.isEmpty()) {
			throw new NotFoundException("No routes between %s and %s".formatted(minDuration, maxDuration));
		}
		return routes.stream().map(routeMapper::toResponse).toList();
	}
	
	@Override
	public List<RouteResponse> getByDistanceLessThan(BigDecimal maxDistance){
		List<Route> routes = routeRepo.findAllByDistanceKMLessThan(maxDistance);
		if(routes.isEmpty()) {
			throw new NotFoundException("No routes with duration less than %s".formatted(maxDistance));
		}
		return routes.stream().map(routeMapper::toResponse).toList();
	}
	
	@Override
	public List<RouteResponse> getByDistanceBetween(BigDecimal minDistance, BigDecimal maxDistance){
		List<Route> routes = routeRepo.findAllByDistanceKMBetween(minDistance, maxDistance);
		if(routes.isEmpty()) {
			throw new NotFoundException("No routes with duration between %s and %s".formatted(minDistance, maxDistance));
		}
		return routes.stream().map(routeMapper::toResponse).toList();
	}
	
	@Override
	public Page<RouteResponse> getByOriginAndDestination(String origin, String destination, Pageable pageable){
		Page<Route> routes = routeRepo.findAllByOriginAndDestination(origin, destination, pageable);
		if (routes.isEmpty()) {
		    throw new NotFoundException("No routes from %s to %s found".formatted(origin, destination));
		}
		return routes.map(routeMapper::toResponse);
	}
}
