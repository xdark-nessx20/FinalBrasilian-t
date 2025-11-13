package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BusDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface BusMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amenities", expression = "java(amenities(request.amenities()))")
    Bus toEntity(BusCreateRequest request);

    default Set<String> amenities(Set<String> amenities) {
        return (amenities == null || amenities.isEmpty()) ? new HashSet<>() : new HashSet<>(amenities);
    }

    BusResponse toResponse(Bus entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "plate", source = "plate")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "amenities", ignore = true)
    void patch(@MappingTarget Bus target, BusUpdateRequest changes);

    @AfterMapping
    default void afterPatch(BusUpdateRequest request, @MappingTarget Bus target) {
        if (request.amenities() != null){
            if (request.amenities().isEmpty()) target.setAmenities(new HashSet<>());
            else target.setAmenities(new HashSet<>(request.amenities()));
        }
    }
}
