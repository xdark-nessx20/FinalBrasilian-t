package co.edu.unimagdalena.finalbrasiliant.api.dto;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.FareRule;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class FareRuleDTOs {

    public record FareRuleCreateRequest(@NotNull Long routeId,
                                  @NotNull Long fromStopId,
                                  @NotNull Long toStopId,
                                  BigDecimal basePrice,
                                  List<String> discounts,
                                  @NotNull DynamicPricing dynamicPricing) implements Serializable {}

    public record FareRuleUpdateRequest(BigDecimal basePrice,
                                        List<String> discounts,
                                        DynamicPricing dynamicPricing) implements Serializable {}

    public record FareRuleResponse(Long id, RouteSummary route, StopSummary fromStop,
                                    StopSummary toStop,BigDecimal basePrice,
                                   List<String> discounts,
                                   DynamicPricing dynamicPricing) implements Serializable {}

    public record RouteSummary(Long id, String routeName) implements Serializable {}
    public record StopSummary(Long id, String name, Integer stopOrder) implements Serializable {}

}
