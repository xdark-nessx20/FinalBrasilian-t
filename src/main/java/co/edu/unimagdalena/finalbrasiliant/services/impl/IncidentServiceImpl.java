package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.IncidentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.EntityType;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.IncidentType;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.IncidentRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.IncidentService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.IncidentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepo;
    private final IncidentMapper mapper;

    @Override
    @Transactional
    public IncidentResponse create(IncidentCreateRequest request) {
        //This is just in case an entity only can have one incident.
        if (incidentRepo.findByEntityTypeAndEntityId(request.entityType(), request.entityId()).isPresent())
            throw new AlreadyExistsException("Already exists an Incident for the %s with id %d".formatted(request.entityType().name(), request.entityId()));
        return mapper.toResponse(incidentRepo.save(mapper.toEntity(request)));
    }

    @Override
    public IncidentResponse get(Long id) {
        return incidentRepo.findById(id).map(mapper::toResponse).orElseThrow(
                () -> new NotFoundException("Incident %d not found".formatted(id))
        );
    }

    @Override
    @Transactional
    public IncidentResponse update(Long id, IncidentUpdateRequest request) {
        var incident = incidentRepo.findById(id).orElseThrow(() -> new NotFoundException("Incident %d not found".formatted(id)));
        mapper.patch(incident, request);
        return mapper.toResponse(incidentRepo.save(incident));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        incidentRepo.deleteById(id);
    }

    @Override
    public Page<IncidentResponse> listByCreatedAt(OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        if (to.isBefore(from)) throw new IllegalArgumentException("End date may not be before start date.");
        return incidentRepo.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pageable).map(mapper::toResponse);
    }

    @Override
    public List<IncidentResponse> listByTypeAndEntityType(IncidentType type, EntityType entityType) {
        if (type == null) return incidentRepo.findByEntityType(entityType).stream().map(mapper::toResponse).toList();
        else if (entityType == null) return incidentRepo.findByType(type).stream().map(mapper::toResponse).toList();
        else return incidentRepo.findByEntityTypeAndType(entityType, type).stream().map(mapper::toResponse).toList();
    }

    @Override
    public Page<IncidentResponse> getAll(Pageable pageable) {
        return incidentRepo.findAll(pageable).map(mapper::toResponse);
    }
}
