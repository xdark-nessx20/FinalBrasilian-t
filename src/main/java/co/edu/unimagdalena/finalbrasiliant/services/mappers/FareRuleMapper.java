package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.FareRuleDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.FareRule;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FareRuleMapper {

    @BeforeMapping
    default void afterToEntity(FareRuleCreateRequest request, @MappingTarget FareRule fareRule) {
        if (request.discounts() != null && !request.discounts().isEmpty()) {
            request.discounts().forEach((key, value) -> {
                fareRule.getDiscounts().put(key, value);
            });
        }
    }

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
    @Mapping(target = "discounts", ignore = true)
    @Mapping(target = "dynamicPricing", source = "dynamicPricing")
    void patch(@MappingTarget FareRule target, FareRuleUpdateRequest changes);

    @AfterMapping
    default void afterPatch(FareRuleUpdateRequest request, @MappingTarget FareRule entity) {
        if (request.discounts() != null){
            if (request.discounts().isEmpty()) entity.getDiscounts().clear();
            else request.discounts().forEach((key, value) -> {
                entity.getDiscounts().put(key, value);
            });
        }
    }
}
