package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.StopDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StopMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    Stop toEntity(StopCreateRequest request);

    @Mapping(target = "routeId", source = "route.id")
    StopResponse toResponse(Stop entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "stopOrder", source = "stopOrder")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lng", source = "lng")
    void patch(@MappingTarget Stop target, StopUpdateRequest changes);
}
