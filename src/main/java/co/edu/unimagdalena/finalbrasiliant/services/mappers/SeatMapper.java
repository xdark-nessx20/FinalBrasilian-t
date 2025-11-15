package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Seat;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SeatMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bus", ignore = true)
    Seat toEntity(SeatCreateRequest request);

    @Mapping(target = "bus_id", source = "bus.id")
    SeatResponse toResponse(Seat seat);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bus", ignore = true)
    void patch(@MappingTarget Seat seat, SeatUpdateRequest request);
}