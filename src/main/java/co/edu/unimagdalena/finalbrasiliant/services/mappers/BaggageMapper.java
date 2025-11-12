package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BaggageDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Baggage;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Ticket;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BaggageMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    @Mapping(target = "tagCode", ignore = true)
    Baggage toEntity(BaggageCreateRequest request);

    @Mapping(target = "ticket", source = "ticket")
    BaggageResponse toResponse(Baggage entity);

    @Mapping(target = "passengerName", source = "ticket.passenger.userName")
    TicketSummary toTicketSummary(Ticket ticket);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "fee", source = "fee")
    void patch(@MappingTarget Baggage target, BaggageUpdateRequest changes);
}
