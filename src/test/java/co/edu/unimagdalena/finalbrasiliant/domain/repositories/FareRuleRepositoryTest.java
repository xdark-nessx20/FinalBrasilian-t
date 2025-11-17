package co.edu.unimagdalena.finalbrasiliant.domain.repositories;



import co.edu.unimagdalena.finalbrasiliant.domain.entities.FareRule;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class FareRuleRepositoryTest extends AbstractRepository {

    @Autowired
    private FareRuleRepository fareRuleRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    private Route route1;
    private Route route2;
    private Stop stop1;
    private Stop stop2;
    private Stop stop3;
    private Stop stop4;
    private FareRule fareRule1;
    private FareRule fareRule2;
    private FareRule fareRule3;
    private FareRule fareRule4;
    private FareRule fareRule5;

    @BeforeEach
    void setUp() {
        fareRuleRepository.deleteAll();
        stopRepository.deleteAll();
        routeRepository.deleteAll();

        route1 = routeRepository.save(Route.builder()
                .code("ZZZZ").routeName("a")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(BigDecimal.valueOf(415.0))
                .durationMin(360)
                .build());

        route2 = routeRepository.save(Route.builder()
                .code("ZZZA").routeName("b")
                .origin("Cali")
                .destination("Cartagena")
                .distanceKM(BigDecimal.valueOf(1100.0))
                .durationMin(900)
                .build());

        stop1 = stopRepository.save(Stop.builder()
                .route(route1)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .build());

        stop2 = stopRepository.save(Stop.builder()
                .route(route1)
                .name("Peaje La Línea")
                .stopOrder(2)
                .build());

        stop3 = stopRepository.save(Stop.builder()
                .route(route1)
                .name("Terminal Medellín")
                .stopOrder(3)
                .build());

        stop4 = stopRepository.save(Stop.builder()
                .route(route2)
                .name("Terminal Cali")
                .stopOrder(1)
                .build());

        fareRule1 = createFareRule(route1, stop1, stop2,
                new BigDecimal("25000.00"), DynamicPricing.ON);

        fareRule2 = createFareRule(route1, stop2, stop3,
                new BigDecimal("30000.00"), DynamicPricing.ON);

        fareRule3 = createFareRule(route1, stop1, stop3,
                new BigDecimal("50000.00"), DynamicPricing.OFF);

        fareRule4 = createFareRule(route2, stop4, stop4,
                new BigDecimal("80000.00"), DynamicPricing.ON);

        fareRule5 = createFareRule(route1, stop2, stop2,
                new BigDecimal("15000.00"), DynamicPricing.OFF);
    }

    private FareRule createFareRule(Route route, Stop fromStop, Stop toStop,
                                    BigDecimal basePrice, DynamicPricing dynamicPricing) {
        return fareRuleRepository.save(FareRule.builder()
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(basePrice)
                .dynamicPricing(dynamicPricing)
                .discounts(new HashMap<>(Map.of("STUDENT", BigDecimal.valueOf(.10),
                        "SENIOR",  BigDecimal.valueOf(.15))))
                .build());
    }

    @Test
    void shouldFindFareRuleByRouteAndStops() {
        Optional<FareRule> result = fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(
                route1.getId(), stop1.getId(), stop2.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(fareRule -> {
                    assertThat(fareRule.getId()).isEqualTo(fareRule1.getId());
                    assertThat(fareRule.getBasePrice()).isEqualByComparingTo(new BigDecimal("25000.00"));
                    assertThat(fareRule.getDynamicPricing()).isEqualTo(DynamicPricing.ON);
                });
    }

    @Test
    void shouldFindAllFareRulesByRoute() {
        List<FareRule> result = fareRuleRepository.findByRoute_Id(route1.getId());

        assertThat(result)
                .hasSize(4)
                .extracting(FareRule::getId)
                .containsExactlyInAnyOrder(
                        fareRule1.getId(), fareRule2.getId(),
                        fareRule3.getId(), fareRule5.getId()
                );
    }

    @Test
    void shouldFindFareRulesByFromStop() {
        List<FareRule> result = fareRuleRepository.findByFromStop_Id(stop1.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(FareRule::getId)
                .containsExactlyInAnyOrder(fareRule1.getId(), fareRule3.getId());
    }

    @Test
    void shouldFindFareRulesByToStop() {
        List<FareRule> result = fareRuleRepository.findByToStop_Id(stop3.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(FareRule::getId)
                .containsExactlyInAnyOrder(fareRule2.getId(), fareRule3.getId());
    }

    @Test
    void shouldFindFareRulesByDynamicPricing() {
        List<FareRule> result = fareRuleRepository.findByDynamicPricing(DynamicPricing.ON);

        assertThat(result)
                .hasSize(3)
                .extracting(FareRule::getId)
                .containsExactlyInAnyOrder(fareRule1.getId(), fareRule2.getId(), fareRule4.getId());
    }

    @Test
    void shouldFindFareRulesByRouteAndDynamicPricing() {
        List<FareRule> result = fareRuleRepository.findByRouteIdAndDynamicPricing(
                route1.getId(), DynamicPricing.OFF);

        assertThat(result)
                .hasSize(2)
                .extracting(FareRule::getId)
                .containsExactlyInAnyOrder(fareRule3.getId(), fareRule5.getId());
    }

    @Test
    void shouldCheckIfFareRuleExists() {
        boolean exists = fareRuleRepository.existsByRouteIdAndFromStopIdAndToStopId(
                route1.getId(), stop1.getId(), stop2.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFareRuleDoesNotExist() {
        boolean exists = fareRuleRepository.existsByRouteIdAndFromStopIdAndToStopId(
                route1.getId(), stop3.getId(), stop1.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindFareRuleByIdWithAllDetails() {
        Optional<FareRule> result = fareRuleRepository.findByIdWithAllDetails(fareRule1.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(fareRule -> {
                    assertThat(fareRule.getId()).isEqualTo(fareRule1.getId());
                    assertThat(fareRule.getRoute())
                            .isNotNull()
                            .satisfies(route -> {
                                assertThat(route.getId()).isEqualTo(route1.getId());
                                assertThat(route.getOrigin()).isEqualTo("Bogotá");
                            });
                    assertThat(fareRule.getFromStop())
                            .isNotNull()
                            .satisfies(stop -> {
                                assertThat(stop.getId()).isEqualTo(stop1.getId());
                                assertThat(stop.getName()).isEqualTo("Terminal Bogotá");
                            });
                    assertThat(fareRule.getToStop())
                            .isNotNull()
                            .satisfies(stop -> {
                                assertThat(stop.getId()).isEqualTo(stop2.getId());
                                assertThat(stop.getName()).isEqualTo("Peaje La Línea");
                            });
                });
    }


}