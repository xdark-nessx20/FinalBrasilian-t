package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatHoldDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.SeatHold;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SeatHoldMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "passenger", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    SeatHold toEntity(SeatHoldCreateRequest request);

    @Mapping(target = "trip", source = "trip")
    @Mapping(target = "passenger", source = "passenger")
    SeatHoldResponse toResponse(SeatHold entity);

    UserSummary toUserSummary(User user);

    @Mapping(target = "status", source = "status")
    TripSummary toTripSummary(Trip trip);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "passenger", ignore = true)
    @Mapping(target = "status", source = "status")
    void patch(@MappingTarget SeatHold target, SeatHoldUpdateRequest changes);
}
