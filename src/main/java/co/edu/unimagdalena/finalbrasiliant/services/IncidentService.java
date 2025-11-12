package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.IncidentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.EntityType;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.IncidentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface IncidentService {
    IncidentResponse create(IncidentCreateRequest request);
    IncidentResponse get(Long id);
    IncidentResponse update(Long id, IncidentUpdateRequest request);
    void delete(Long id);

    Page<IncidentResponse> listByCreatedAt(OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    List<IncidentResponse> listByTypeAndEntityType(IncidentType type, EntityType entityType);
    Page<IncidentResponse> getAll(Pageable pageable);
}
