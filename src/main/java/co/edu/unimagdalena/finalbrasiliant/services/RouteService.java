package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.*;

public interface RouteService {
	RouteResponse create(RouteCreateRequest request);
	RouteResponse get(Long id);
	RouteResponse update(Long id, RouteUpdateRequest request);
	RouteResponse delete(Long id);
	
	RouteResponse getByCode(String code);
	RouteResponse getByRouteName(String routeName);
	RouteResponse getByOriginAndDestination(String origin, String destination);
}
