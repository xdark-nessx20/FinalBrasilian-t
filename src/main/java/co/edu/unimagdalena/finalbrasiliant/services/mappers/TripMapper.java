package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TripMapper {
	@Mapping(target = "id", ignore = true)
    @Mapping(target = "bus.id", source = "bus_id")
	@Mapping(target = "route.id", source = "route_id")
	@Mapping(target = "status", ignore = true)
	Trip toEntity(TripCreateRequest request);
	
	@Mapping(target = "bus_id", source = "bus.id")
	@Mapping(target = "route_id", source = "route.id")
	TripResponse toResponse(Trip trip);
	
	@Mapping(target = "id", ignore = true)
    @Mapping(target = "bus.id", source = "bus_id")
	@Mapping(target = "route.id", source = "route_id")
	void updateEntityFromRequest(TripUpdateRequest request, @MappingTarget Trip trip);
}
