package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.FareRuleDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;

import java.util.List;

public interface FareRuleService {

    FareRuleResponse create(FareRuleCreateRequest request);
    FareRuleResponse get(Long id);
    FareRuleResponse update(Long id, FareRuleUpdateRequest request);
    void delete(Long id);

    FareRuleResponse getByRouteAndStops(Long routeId, Long fromStopId, Long toStopId);
    List<FareRuleResponse> listByRoute(Long routeId);
    List<FareRuleResponse> listByFromStop(Long fromStopId);
    List<FareRuleResponse> listByToStop(Long toStopId);
    List<FareRuleResponse> listByDynamicPricing(DynamicPricing dynamicPricing);
    List<FareRuleResponse> listByRouteAndDynamicPricing(Long routeId, DynamicPricing dynamicPricing);
    boolean existsByRouteAndStops(Long routeId, Long fromStopId, Long toStopId);
}
