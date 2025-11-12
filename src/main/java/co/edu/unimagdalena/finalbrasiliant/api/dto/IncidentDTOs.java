package co.edu.unimagdalena.finalbrasiliant.api.dto;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.EntityType;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.IncidentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class IncidentDTOs {
    public record IncidentCreateRequest(@NotNull EntityType entityType, @NotNull Long entityId, @NotNull IncidentType type,
                                        @Size(max = 255) String note) implements Serializable {}
    public record IncidentUpdateRequest(@Size(max = 255) String note, Long entityId) implements Serializable {}
    public record IncidentResponse(Long id, EntityType entityType, Long entityId, IncidentType type, OffsetDateTime createdAt,
                                   String note) implements Serializable {}
}
