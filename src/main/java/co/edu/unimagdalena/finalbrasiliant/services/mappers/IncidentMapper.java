package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.IncidentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Incident;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IncidentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Incident toEntity(IncidentCreateRequest request);

    IncidentResponse toResponse(Incident entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "entityId", source = "entityId")
    @Mapping(target = "note", source = "note")
    void patch(@MappingTarget Incident target, IncidentUpdateRequest changes);
}
