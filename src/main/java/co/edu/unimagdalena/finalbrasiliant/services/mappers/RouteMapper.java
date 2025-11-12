package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RouteMapper {
    @Mapping(target = "id", ignore = true)
    Route toEntity(RouteCreateRequest request);
    
    
    RouteResponse toResponse(Route route);
    
    @Mapping(target = "id", ignore = true)
    void patch(@MappingTarget Route route, RouteUpdateRequest request);
}