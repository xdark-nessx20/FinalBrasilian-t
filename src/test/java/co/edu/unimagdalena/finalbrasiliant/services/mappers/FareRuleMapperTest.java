package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.FareRuleDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.FareRule;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FareRuleMapperTest {

    private final FareRuleMapper mapper = Mappers.getMapper(FareRuleMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        // Given
        var discounts = List.of("STUDENT:10", "SENIOR:15");
        var request = new FareRuleCreateRequest(
                1L,
                2L,
                3L,
                new BigDecimal("50000.00"),
                discounts,
                DynamicPricing.ON
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getBasePrice()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(entity.getDiscounts()).containsExactlyElementsOf(discounts);
        assertThat(entity.getDynamicPricing()).isEqualTo(DynamicPricing.ON);

        // Campos ignorados
        assertThat(entity.getId()).isNull();
        assertThat(entity.getRoute()).isNull();
        assertThat(entity.getFromStop()).isNull();
        assertThat(entity.getToStop()).isNull();
    }


    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var route = Route.builder()
                .id(1L)
                .origin("Bogotá")
                .destination("Medellín")
                .build();

        var fromStop = Stop.builder()
                .id(2L)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .build();

        var toStop = Stop.builder()
                .id(3L)
                .name("Terminal Medellín")
                .stopOrder(3)
                .build();

        var discounts = List.of("STUDENT:10", "SENIOR:15");
        var fareRule = FareRule.builder()
                .id(10L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000.00"))
                .discounts(discounts)
                .dynamicPricing(DynamicPricing.ON)
                .build();

        // When
        var response = mapper.toResponse(fareRule);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.basePrice()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(response.discounts()).containsExactlyElementsOf(discounts);
        assertThat(response.dynamicPricing()).isEqualTo(DynamicPricing.ON);

        // Route summary
        assertThat(response.route().id()).isEqualTo(1L);
        assertThat(response.route().routeName()).isEqualTo("Bogotá"); // Mapped from origin

        // From stop summary
        assertThat(response.fromStop().id()).isEqualTo(2L);
        assertThat(response.fromStop().name()).isEqualTo("Terminal Bogotá");
        assertThat(response.fromStop().stopOrder()).isEqualTo(1);

        // To stop summary
        assertThat(response.toStop().id()).isEqualTo(3L);
        assertThat(response.toStop().name()).isEqualTo("Terminal Medellín");
        assertThat(response.toStop().stopOrder()).isEqualTo(3);
    }

    @Test
    void toRouteSummary_shouldMapRouteOriginToRouteName() {
        // Given
        var route = Route.builder()
                .id(5L)
                .origin("Cali")
                .destination("Cartagena")
                .build();

        // When
        var summary = mapper.toRouteSummary(route);

        // Then
        assertThat(summary.id()).isEqualTo(5L);
        assertThat(summary.routeName()).isEqualTo("Cali"); // origin → routeName
    }


    @Test
    void toStopSummary_shouldMapStop() {
        // Given
        var stop = Stop.builder()
                .id(7L)
                .name("Peaje La Línea")
                .stopOrder(2)
                .build();

        // When
        var summary = mapper.toStopSummary(stop);

        // Then
        assertThat(summary.id()).isEqualTo(7L);
        assertThat(summary.name()).isEqualTo("Peaje La Línea");
        assertThat(summary.stopOrder()).isEqualTo(2);
    }


    @Test
    void patch_shouldUpdateAllFields() {
        // Given
        var fareRule = FareRule.builder()
                .id(10L)
                .basePrice(new BigDecimal("50000.00"))
                .discounts(List.of("STUDENT:10"))
                .dynamicPricing(DynamicPricing.OFF)
                .build();

        var updateRequest = new FareRuleUpdateRequest(
                new BigDecimal("60000.00"),
                List.of("STUDENT:15", "SENIOR:20"),
                DynamicPricing.ON
        );

        // When
        mapper.patch(fareRule, updateRequest);

        // Then
        assertThat(fareRule.getBasePrice()).isEqualByComparingTo(new BigDecimal("60000.00"));
        assertThat(fareRule.getDiscounts()).containsExactly("STUDENT:15", "SENIOR:20");
        assertThat(fareRule.getDynamicPricing()).isEqualTo(DynamicPricing.ON);
        assertThat(fareRule.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFields() {
        // Given
        var fareRule = FareRule.builder()
                .id(10L)
                .basePrice(new BigDecimal("50000.00"))
                .discounts(List.of("STUDENT:10"))
                .dynamicPricing(DynamicPricing.ON)
                .build();

        var updateRequest = new FareRuleUpdateRequest(null, null, null);

        // When
        mapper.patch(fareRule, updateRequest);

        // Then
        assertThat(fareRule.getBasePrice()).isEqualByComparingTo(new BigDecimal("50000.00")); // No cambió
        assertThat(fareRule.getDiscounts()).containsExactly("STUDENT:10"); // No cambió
        assertThat(fareRule.getDynamicPricing()).isEqualTo(DynamicPricing.ON); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyBasePrice() {
        // Given
        var fareRule = FareRule.builder()
                .id(10L)
                .basePrice(new BigDecimal("50000.00"))
                .discounts(List.of("STUDENT:10"))
                .dynamicPricing(DynamicPricing.ON)
                .build();

        var updateRequest = new FareRuleUpdateRequest(new BigDecimal("75000.00"), null, null);

        // When
        mapper.patch(fareRule, updateRequest);

        // Then
        assertThat(fareRule.getBasePrice()).isEqualByComparingTo(new BigDecimal("75000.00")); // Cambió
        assertThat(fareRule.getDiscounts()).containsExactly("STUDENT:10"); // No cambió
        assertThat(fareRule.getDynamicPricing()).isEqualTo(DynamicPricing.ON); // No cambió
    }

    @Test
    void patch_shouldNotModifyIdOrRelations() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(2L).build();
        var toStop = Stop.builder().id(3L).build();

        var fareRule = FareRule.builder()
                .id(10L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000.00"))
                .discounts(List.of("STUDENT:10"))
                .dynamicPricing(DynamicPricing.ON)
                .build();

        var updateRequest = new FareRuleUpdateRequest(
                new BigDecimal("70000.00"),
                List.of("SENIOR:20"),
                DynamicPricing.OFF
        );

        // When
        mapper.patch(fareRule, updateRequest);

        // Then
        assertThat(fareRule.getId()).isEqualTo(10L); // No cambió
        assertThat(fareRule.getRoute()).isEqualTo(route); // No cambió
        assertThat(fareRule.getFromStop()).isEqualTo(fromStop); // No cambió
        assertThat(fareRule.getToStop()).isEqualTo(toStop); // No cambió
    }
}

