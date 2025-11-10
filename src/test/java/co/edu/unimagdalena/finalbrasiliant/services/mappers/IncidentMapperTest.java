package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.IncidentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Incident;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.EntityType;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.IncidentType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentMapperTest {

    private final IncidentMapper mapper = Mappers.getMapper(IncidentMapper.class);

    @Test
    void toEntity_shouldMapCreateRequestWithNote() {
        // Given
        var request = new IncidentCreateRequest(
                EntityType.TRIP,
                1L,
                IncidentType.SECURITY,
                "Pasajero sospechoso"
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getEntityType()).isEqualTo(EntityType.TRIP);
        assertThat(entity.getEntityId()).isEqualTo(1L);
        assertThat(entity.getType()).isEqualTo(IncidentType.SECURITY);
        assertThat(entity.getNote()).isEqualTo("Pasajero sospechoso");
        assertThat(entity.getId()).isNull(); // Ignored by mapper
        assertThat(entity.getCreatedAt()).isNull(); // Ignored by mapper
    }

    @Test
    void toEntity_shouldMapCreateRequestWithNullNote() {
        // Given
        var request = new IncidentCreateRequest(
                EntityType.TICKET,
                2L,
                IncidentType.OVERBOOK,
                null
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getEntityType()).isEqualTo(EntityType.TICKET);
        assertThat(entity.getEntityId()).isEqualTo(2L);
        assertThat(entity.getType()).isEqualTo(IncidentType.OVERBOOK);
        assertThat(entity.getNote()).isNull();
    }

    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var createdAt = OffsetDateTime.now();
        var incident = Incident.builder()
                .id(10L)
                .entityType(EntityType.PARCEL)
                .entityId(3L)
                .type(IncidentType.DELIVERY_FAIL)
                .createdAt(createdAt)
                .note("Paquete no entregado")
                .build();

        // When
        var response = mapper.toResponse(incident);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.entityType()).isEqualTo(EntityType.PARCEL);
        assertThat(response.entityId()).isEqualTo(3L);
        assertThat(response.type()).isEqualTo(IncidentType.DELIVERY_FAIL);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.note()).isEqualTo("Paquete no entregado");
    }

    @Test
    void toResponse_shouldMapEntityWithNullNote() {
        // Given
        var incident = Incident.builder()
                .id(5L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.VEHICLE)
                .createdAt(OffsetDateTime.now())
                .note(null)
                .build();

        // When
        var response = mapper.toResponse(incident);

        // Then
        assertThat(response.note()).isNull();
    }

    @Test
    void patch_shouldUpdateNoteAndEntityId() {
        // Given
        var incident = Incident.builder()
                .id(10L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.SECURITY)
                .note("Nota original")
                .build();

        var updateRequest = new IncidentUpdateRequest("Nota actualizada", 2L);

        // When
        mapper.patch(incident, updateRequest);

        // Then
        assertThat(incident.getNote()).isEqualTo("Nota actualizada");
        assertThat(incident.getEntityId()).isEqualTo(2L);
        assertThat(incident.getId()).isEqualTo(10L); // No cambió
        assertThat(incident.getEntityType()).isEqualTo(EntityType.TRIP); // No cambió
        assertThat(incident.getType()).isEqualTo(IncidentType.SECURITY); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullNote() {
        // Given
        var incident = Incident.builder()
                .id(10L)
                .entityType(EntityType.TICKET)
                .entityId(5L)
                .type(IncidentType.OVERBOOK)
                .note("Nota original")
                .build();

        var updateRequest = new IncidentUpdateRequest(null, null);

        // When
        mapper.patch(incident, updateRequest);

        // Then
        assertThat(incident.getNote()).isEqualTo("Nota original"); // No cambió
        assertThat(incident.getEntityId()).isEqualTo(5L); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyNote() {
        // Given
        var incident = Incident.builder()
                .id(10L)
                .entityType(EntityType.PARCEL)
                .entityId(7L)
                .type(IncidentType.DELIVERY_FAIL)
                .note("Nota vieja")
                .build();

        var updateRequest = new IncidentUpdateRequest("Nueva nota", null);

        // When
        mapper.patch(incident, updateRequest);

        // Then
        assertThat(incident.getNote()).isEqualTo("Nueva nota"); // Cambió
        assertThat(incident.getEntityId()).isEqualTo(7L); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyEntityId() {
        // Given
        var incident = Incident.builder()
                .id(10L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.VEHICLE)
                .note("Nota original")
                .build();

        var updateRequest = new IncidentUpdateRequest(null, 99L);

        // When
        mapper.patch(incident, updateRequest);

        // Then
        assertThat(incident.getEntityId()).isEqualTo(99L); // Cambió
        assertThat(incident.getNote()).isEqualTo("Nota original"); // No cambió
    }

    @Test
    void patch_shouldNotModifyIdTypeOrCreatedAt() {
        // Given
        var createdAt = OffsetDateTime.now().minusDays(1);
        var incident = Incident.builder()
                .id(10L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.SECURITY)
                .createdAt(createdAt)
                .note("Original")
                .build();

        var updateRequest = new IncidentUpdateRequest("Updated", 2L);

        // When
        mapper.patch(incident, updateRequest);

        // Then
        assertThat(incident.getId()).isEqualTo(10L); // No cambió
        assertThat(incident.getEntityType()).isEqualTo(EntityType.TRIP); // No cambió
        assertThat(incident.getType()).isEqualTo(IncidentType.SECURITY); // No cambió
        assertThat(incident.getCreatedAt()).isEqualTo(createdAt); // No cambió
    }
}
