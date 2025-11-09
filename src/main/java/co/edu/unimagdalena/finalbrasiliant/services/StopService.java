package co.edu.unimagdalena.finalbrasiliant.services;
import co.edu.unimagdalena.finalbrasiliant.api.dto.StopDTOs.*;

import java.util.List;

public interface StopService {

    StopResponse create(StopCreateRequest request);
    StopResponse get(Long id);
    StopResponse update(Long id, StopUpdateRequest request);
    void delete(Long id);

    List<StopResponse> listByRoute(Long routeId);
    List<StopResponse> listByName(String name);
    StopResponse getFirstStop(Long routeId);
    StopResponse getLastStop(Long routeId);
    StopResponse getByRouteAndOrder(Long routeId, Integer stopOrder);
    List<StopResponse> listByRouteAndOrderRange(Long routeId, Integer startOrder, Integer endOrder);
    long countByRoute(Long routeId);
    boolean existsByRouteAndName(Long routeId, String name);

}
