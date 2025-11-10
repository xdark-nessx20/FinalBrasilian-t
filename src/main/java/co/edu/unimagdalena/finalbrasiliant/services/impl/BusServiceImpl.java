package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BusDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.BusService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.BusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BusServiceImpl implements BusService {

    private final BusRepository busRepo;
    private final BusMapper mapper;

    @Override
    @Transactional
    public BusResponse create(BusCreateRequest request) {
        if (busRepo.findByPlate(request.plate()).isPresent()) {
            throw new AlreadyExistsException("Already exists bus with plate %s".formatted(request.plate()));
        }
        var bus = mapper.toEntity(request);
        var saved = busRepo.save(bus);
        return mapper.toResponse(saved);
    }

    @Override
    public BusResponse get(Long id) {
        return busRepo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(id)));
    }

    @Override
    @Transactional
    public BusResponse update(Long id, BusUpdateRequest request) {
        var bus = busRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(id)));

        if (request.plate() != null && !request.plate().equals(bus.getPlate())) {
            if (busRepo.findByPlate(request.plate()).isPresent()) {
                throw new AlreadyExistsException("Already exists bus with plate %s".formatted(request.plate()));
            }
        }
        mapper.patch(bus, request);
        return mapper.toResponse(busRepo.save(bus));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!busRepo.existsById(id)) {
            throw new NotFoundException("Bus %d not found".formatted(id));
        }
        busRepo.deleteById(id);
    }

    @Override
    public BusResponse getByPlate(String plate) {
        return busRepo.findByPlate(plate)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Bus with plate %s not found".formatted(plate)));
    }

    @Override
    public List<BusResponse> listByStatus(BusStatus status) {
        return busRepo.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<BusResponse> listByCapacityGreaterThanEqual(Integer capacity) {
        return busRepo.findByCapacityGreaterThanEqual(capacity)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<BusResponse> listByAmenities(Set<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return List.of();
        }
        return busRepo.findByAmenities(amenities, amenities.size())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
