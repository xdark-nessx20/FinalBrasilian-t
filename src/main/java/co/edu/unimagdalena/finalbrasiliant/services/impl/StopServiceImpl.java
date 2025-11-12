package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.StopDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.StopService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.StopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StopServiceImpl implements StopService {


    private final StopRepository stopRepo;
    private final RouteRepository routeRepo;
    private final StopMapper mapper;

    @Override
    @Transactional
    public StopResponse create(StopCreateRequest request) {
        var route = routeRepo.findById(request.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found.".formatted(request.routeId())));

        if (stopRepo.existsByRouteIdAndNameIgnoreCase(request.routeId(), request.name())) {
            throw new AlreadyExistsException("Stop '%s' already exists in route %d."
                    .formatted(request.name(), request.routeId()));
        }

        var stop = mapper.toEntity(request);
        stop.setRoute(route);
        return mapper.toResponse(stopRepo.save(stop));
    }

    @Override
    public StopResponse get(Long id) {
        return stopRepo.findByIdWithRoute(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Stop %d not found.".formatted(id)));
    }

    @Override
    @Transactional
    public StopResponse update(Long id, StopUpdateRequest request) {
        var stop = stopRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stop %d not found.".formatted(id)));

        if (request.name() != null && !request.name().equalsIgnoreCase(stop.getName())) {
            if (stopRepo.existsByRouteIdAndNameIgnoreCase(stop.getRoute().getId(), request.name())) {
                throw new AlreadyExistsException("Another stop with name '%s' already exists in route %d."
                        .formatted(request.name(), stop.getRoute().getId()));
            }
        }

        mapper.patch(stop, request);
        return mapper.toResponse(stopRepo.save(stop));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!stopRepo.existsById(id)) {
            throw new NotFoundException("Stop %d not found.".formatted(id));
        }
        stopRepo.deleteById(id);
    }


    @Override
    public List<StopResponse> listByRoute(Long routeId) {
        routeRepo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route %d not found.".formatted(routeId)));
        return stopRepo.findByRoute_IdOrderByStopOrderAsc(routeId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<StopResponse> listByName(String name) {
        return stopRepo.findByNameContainingIgnoreCase(name)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public StopResponse getFirstStop(Long routeId) {
        routeRepo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route %d not found.".formatted(routeId)));
        return stopRepo.findFirstByRoute_IdOrderByStopOrderAsc(routeId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("No stops found for route %d.".formatted(routeId)));
    }

    @Override
    public StopResponse getLastStop(Long routeId) {
        routeRepo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route %d not found.".formatted(routeId)));
        return stopRepo.findFirstByRoute_IdOrderByStopOrderDesc(routeId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("No stops found for route %d.".formatted(routeId)));
    }

    @Override
    public StopResponse getByRouteAndOrder(Long routeId, Integer stopOrder) {
        return stopRepo.findByRouteIdAndStopOrder(routeId, stopOrder)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException(
                        "Stop with order %d not found in route %d.".formatted(stopOrder, routeId)
                ));
    }

    @Override
    public List<StopResponse> listByRouteAndOrderRange(Long routeId, Integer startOrder, Integer endOrder) {
        return stopRepo.findByRouteIdAndStopOrderBetween(routeId, startOrder, endOrder)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public long countByRoute(Long routeId) {
        routeRepo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route %d not found.".formatted(routeId)));
        return stopRepo.countByRoute_Id(routeId);
    }

    @Override
    public boolean existsByRouteAndName(Long routeId, String name) {
        return stopRepo.existsByRouteIdAndNameIgnoreCase(routeId, name);
    }

}
