package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TicketDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Ticket;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    Ticket toEntity(TicketCreateRequest request);

    TicketResponse toResponse(Ticket entity);
    UserSummary toUserSummary(User user);
    StopSummary toStopSummary(Stop stop);

    @Mapping(target = "busPlate", source = "trip.bus.plate")
    TripSummary toTripSummary(Trip trip);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "status", source = "status")
    void patch(@MappingTarget Ticket target, TicketUpdateRequest changes);
}
