package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.FareRuleDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.FareRuleRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.FareRuleService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.FareRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository fareRuleRepo;
    private final RouteRepository routeRepo;
    private final StopRepository stopRepo;
    private final FareRuleMapper mapper;

    @Override
    @Transactional
    public FareRuleResponse create(FareRuleCreateRequest request) {
        var route = routeRepo.findById(request.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(request.routeId())));

        var fromStop = stopRepo.findById(request.fromStopId())
                .orElseThrow(() -> new NotFoundException("Origin stop %d not found".formatted(request.fromStopId())));
        if (!fromStop.getRoute().getId().equals(route.getId())) {
            throw new IllegalArgumentException("Origin stop %d not exist on route %d".formatted(request.fromStopId(), request.routeId()));
        }

        var toStop = stopRepo.findById(request.toStopId())
                .orElseThrow(() -> new NotFoundException("Destination stop %d not found".formatted(request.toStopId())));
        if (!toStop.getRoute().getId().equals(route.getId())) {
            throw new IllegalArgumentException("Destination stop %d not exist on route %d".formatted(request.toStopId(), request.routeId()));
        }

        if (fromStop.getStopOrder() >= toStop.getStopOrder()) {
            throw new IllegalArgumentException("Origin stop must come before destination stop in the route order");
        }

        if (fareRuleRepo.existsByRouteIdAndFromStopIdAndToStopId(request.routeId(), request.fromStopId(), request.toStopId())) {
            throw new AlreadyExistsException("Already exist a fare for route %d from stop %d to stop %d"
                    .formatted(request.routeId(), request.fromStopId(), request.toStopId()));
        }

        var fareRule = mapper.toEntity(request);
        fareRule.setRoute(route);
        fareRule.setFromStop(fromStop);
        fareRule.setToStop(toStop);

        var saved = fareRuleRepo.save(fareRule);
        return mapper.toResponse(saved);
    }

    @Override
    public FareRuleResponse get(Long id) {
        return fareRuleRepo.findByIdWithAllDetails(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Fare %d not found".formatted(id)));
    }


    @Override
    @Transactional
    public FareRuleResponse update(Long id, FareRuleUpdateRequest request) {
        var fareRule = fareRuleRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Fare %d not found".formatted(id)));

        mapper.patch(fareRule, request);
        return mapper.toResponse(fareRuleRepo.save(fareRule));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!fareRuleRepo.existsById(id)) {
            throw new NotFoundException("Fare %d not found".formatted(id));
        }
        fareRuleRepo.deleteById(id);
    }

    @Override
    public FareRuleResponse getByRouteAndStops(Long routeId, Long fromStopId, Long toStopId) {
        routeRepo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(routeId)));

        stopRepo.findById(fromStopId)
                .orElseThrow(() -> new NotFoundException("Origin stop %d no encontrada".formatted(fromStopId)));
        stopRepo.findById(toStopId)
                .orElseThrow(() -> new NotFoundException("Destination stop %d no encontrada".formatted(toStopId)));

        return fareRuleRepo.findByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("No fare exists for the requested route."));
    }

    @Override
    public List<FareRuleResponse> listByRoute(Long routeId) {
        routeRepo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(routeId)));

        return fareRuleRepo.findByRoute_Id(routeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<FareRuleResponse> listByFromStop(Long fromStopId) {
        stopRepo.findById(fromStopId)
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(fromStopId)));

        return fareRuleRepo.findByFromStop_Id(fromStopId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<FareRuleResponse> listByToStop(Long toStopId) {
        stopRepo.findById(toStopId)
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(toStopId)));

        return fareRuleRepo.findByToStop_Id(toStopId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<FareRuleResponse> listByDynamicPricing(DynamicPricing dynamicPricing) {
        return fareRuleRepo.findByDynamicPricing(dynamicPricing)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<FareRuleResponse> listByRouteAndDynamicPricing(Long routeId, DynamicPricing dynamicPricing) {
        routeRepo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(routeId)));

        return fareRuleRepo.findByRouteIdAndDynamicPricing(routeId, dynamicPricing)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public boolean existsByRouteAndStops(Long routeId, Long fromStopId, Long toStopId) {
        return fareRuleRepo.existsByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId);
    }
}
