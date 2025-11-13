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
    @Mapping(target = "bus", ignore = true)
	@Mapping(target = "route", ignore = true)
	@Mapping(target = "status", ignore = true)
	Trip toEntity(TripCreateRequest request);
	
	@Mapping(target = "bus_id", source = "bus.id")
	@Mapping(target = "route_id", source = "route.id")
	TripResponse toResponse(Trip trip);
	
	@Mapping(target = "id", ignore = true)
    @Mapping(target = "bus", ignore = true)
	@Mapping(target = "route", ignore = true)
	void patch(@MappingTarget Trip trip, TripUpdateRequest request);
}
