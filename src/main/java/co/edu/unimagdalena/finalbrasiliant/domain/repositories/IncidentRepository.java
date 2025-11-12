package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.EntityType;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Incident;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.IncidentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident,Long> {
    Page<Incident> findByCreatedAtBetweenOrderByCreatedAtDesc(OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    List<Incident> findByEntityType(EntityType entityType);
    List<Incident> findByType(IncidentType type);
    List<Incident> findByEntityTypeAndType(EntityType entityType, IncidentType type);
    long countByType(IncidentType type);
    long countByEntityType(EntityType entityType);
    Optional<Incident> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
}
