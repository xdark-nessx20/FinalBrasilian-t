package co.edu.unimagdalena.finalbrasiliant.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;

import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.*;

public interface RouteService {
	RouteResponse create(RouteCreateRequest request);
	RouteResponse get(Long id);
	RouteResponse update(Long id, RouteUpdateRequest request);
	RouteResponse delete(Long id);
	
	RouteResponse getByCode(String code);
	RouteResponse getByRouteName(String routeName);
	List<RouteResponse> getByOrigin(String origin);
	List<RouteResponse> getByDestination(String destination);
	List<RouteResponse> getByMinDurationGreaterThan(Integer minDuration);
	List<RouteResponse> getByDurationBetween(Integer minDuration, Integer maxDuration);
	List<RouteResponse> getByDistanceLessThan(BigDecimal maxDistance);
	List<RouteResponse> getByDistanceBetween(BigDecimal minDistance, BigDecimal maxDistance);
	Page<RouteResponse> getByOriginAndDestination(String origin, String destination, Pageable pageable);

}
