package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ParcelDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Parcel;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ParcelMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deliveryOTP", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    Parcel toEntity(ParcelCreateRequest request);

    ParcelResponse toResponse(Parcel entity);
    StopSummary toStopSummary(Stop stop);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deliveryOTP", ignore = true)
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    void patch(@MappingTarget Parcel target, ParcelUpdateRequest changes);
}
