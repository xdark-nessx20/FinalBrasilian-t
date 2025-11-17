package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ConfigDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Config;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ConfigMapper {
    Config toEntity(ConfigCreateRequest config);

    @BeanMapping(ignoreByDefault = true)
    void patch(@MappingTarget Config target, ConfigUpdateRequest change);

    ConfigResponse toResponse(Config entity);
}
