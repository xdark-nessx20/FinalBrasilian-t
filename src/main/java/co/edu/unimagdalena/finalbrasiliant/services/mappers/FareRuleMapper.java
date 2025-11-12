package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.FareRuleDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.FareRule;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FareRuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    FareRule toEntity(FareRuleCreateRequest request);

    @Mapping(target = "route", source = "route")
    @Mapping(target = "fromStop", source = "fromStop")
    @Mapping(target = "toStop", source = "toStop")
    FareRuleResponse toResponse(FareRule entity);

    @Mapping(target = "routeName", source = "origin")
    RouteSummary toRouteSummary(Route route);

    StopSummary toStopSummary(Stop stop);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "discounts", source = "discounts")
    @Mapping(target = "dynamicPricing", source = "dynamicPricing")
    void patch(@MappingTarget FareRule target, FareRuleUpdateRequest changes);
}
