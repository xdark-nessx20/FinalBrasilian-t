package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BusDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BusMapper {

    @Mapping(target = "id", ignore = true)
    Bus toEntity(BusCreateRequest request);

    BusResponse toResponse(Bus entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "plate", source = "plate")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "amenities", source = "amenities")
    @Mapping(target = "status", source = "status")
    void patch(@MappingTarget Bus target, BusUpdateRequest changes);
}
