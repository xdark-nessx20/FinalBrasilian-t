package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.AssignmentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Assignment;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "checkListOk", source = "checkListOk")
    Assignment toEntity(AssignmentCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "checkListOk", source = "checkListOk")
    //Driver and Dispatcher have been ignored 'cause in the DTO, they are IDs. So, we'll set up them in service if they're not null
    void patch(@MappingTarget Assignment target, AssignmentUpdateRequest changes);

    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "driver", source = "driver")
    @Mapping(target = "dispatcher", source = "dispatcher")
    AssignmentResponse toResponse(Assignment entity);

    UserSummary toUserSummary(User user);
}
