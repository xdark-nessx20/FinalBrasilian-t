package co.edu.unimagdalena.finalbrasiliant.domain.repositories;


import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StopRepositoryTest extends AbstractRepository{
    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Route route1;
    private Route route2;
    private Stop stop1;
    private Stop stop2;
    private Stop stop3;
    private Stop stop4;
    private Stop stop5;
    private Stop stop6;

    @BeforeEach
    void setUp() {
        stopRepository.deleteAll();
        routeRepository.deleteAll();

        route1 = routeRepository.save(Route.builder()
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(BigDecimal.valueOf(415.0))
                .durationMin(360)
                .build());

        route2 = routeRepository.save(Route.builder()
                .origin("Cali")
                .destination("Cartagena")
                .distanceKM(BigDecimal.valueOf(1100.0))
                .durationMin(900)
                .build());

        stop1 = createStop(route1, "Terminal Bogotá", 1, 4.6097, -74.0817);
        stop2 = createStop(route1, "Peaje La Línea", 2, 4.5000, -74.5000);
        stop3 = createStop(route1, "Terminal Medellín", 3, 6.2442, -75.5812);
        stop4 = createStop(route2, "Terminal Cali", 1, 3.4516, -76.5320);
        stop5 = createStop(route2, "Montería", 2, 8.7479, -75.8814);
        stop6 = createStop(route2, "Terminal Cartagena", 3, 10.3910, -75.4794);
    }

    private Stop createStop(Route route, String name, Integer stopOrder,
                            Double lat, Double lng) {
        return stopRepository.save(Stop.builder()
                .route(route)
                .name(name)
                .stopOrder(stopOrder)
                .lat(lat)
                .lng(lng)
                .build());
    }

    @Test
    void shouldFindStopsByRouteIdOrderedByStopOrder() {
        List<Stop> result = stopRepository.findByRoute_IdOrderByStopOrderAsc(route1.getId());

        assertThat(result)
                .hasSize(3)
                .extracting(Stop::getId)
                .containsExactly(stop1.getId(), stop2.getId(), stop3.getId());
    }

    @Test
    void shouldFindStopsByNameContainingIgnoreCase() {
        List<Stop> result = stopRepository.findByNameContainingIgnoreCase("terminal");

        assertThat(result)
                .hasSize(4)
                .extracting(Stop::getId)
                .containsExactlyInAnyOrder(
                        stop1.getId(), stop3.getId(), stop4.getId(), stop6.getId()
                );
    }

    @Test
    void shouldFindStopsByPartialNameIgnoreCase() {
        List<Stop> result = stopRepository.findByNameContainingIgnoreCase("LÍNEA");

        assertThat(result)
                .hasSize(1)
                .extracting(Stop::getId)
                .containsExactly(stop2.getId());
    }

    @Test
    void shouldFindFirstStopByRouteOrderedAsc() {
        Optional<Stop> result = stopRepository.findFirstByRoute_IdOrderByStopOrderAsc(route1.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(stop -> {
                    assertThat(stop.getId()).isEqualTo(stop1.getId());
                    assertThat(stop.getName()).isEqualTo("Terminal Bogotá");
                    assertThat(stop.getStopOrder()).isEqualTo(1);
                });
    }

    @Test
    void shouldFindLastStopByRouteOrderedDesc() {
        Optional<Stop> result = stopRepository.findFirstByRoute_IdOrderByStopOrderDesc(route2.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(stop -> {
                    assertThat(stop.getId()).isEqualTo(stop6.getId());
                    assertThat(stop.getName()).isEqualTo("Terminal Cartagena");
                    assertThat(stop.getStopOrder()).isEqualTo(3);
                });
    }

    @Test
    void shouldCountStopsByRoute() {
        long count = stopRepository.countByRoute_Id(route1.getId());

        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldCheckIfStopExistsByRouteAndName() {
        boolean exists = stopRepository.existsByRouteIdAndNameIgnoreCase(
                route1.getId(), "peaje la línea");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenStopDoesNotExist() {
        boolean exists = stopRepository.existsByRouteIdAndNameIgnoreCase(
                route1.getId(), "Terminal Inexistente");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindStopByRouteAndStopOrder() {
        Optional<Stop> result = stopRepository.findByRouteIdAndStopOrder(
                route1.getId(), 2);

        assertThat(result).isPresent()
                .hasValueSatisfying(stop -> {
                    assertThat(stop.getId()).isEqualTo(stop2.getId());
                    assertThat(stop.getName()).isEqualTo("Peaje La Línea");
                });
    }

    @Test
    void shouldFindStopsByStopOrderBetween() {
        List<Stop> result = stopRepository.findByRouteIdAndStopOrderBetween(
                route2.getId(), 1, 2);

        assertThat(result)
                .hasSize(2)
                .extracting(Stop::getId)
                .containsExactlyInAnyOrder(stop4.getId(), stop5.getId());
    }

    @Test
    void shouldFindStopByIdWithRoute() {
        Optional<Stop> result = stopRepository.findByIdWithRoute(stop1.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(stop -> {
                    assertThat(stop.getId()).isEqualTo(stop1.getId());
                    assertThat(stop.getRoute())
                            .isNotNull()
                            .satisfies(route -> {
                                assertThat(route.getId()).isEqualTo(route1.getId());
                                assertThat(route.getOrigin()).isEqualTo("Bogotá");
                                assertThat(route.getDestination()).isEqualTo("Medellín");
                            });
                });
    }

    @Test
    void shouldReturnEmptyWhenFindingNonExistentStop() {
        Optional<Stop> result = stopRepository.findByRouteIdAndStopOrder(
                route1.getId(), 99);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindStopsWithCoordinates() {
        List<Stop> allStops = stopRepository.findAll();

        assertThat(allStops)
                .hasSize(6)
                .allMatch(stop -> stop.getLat() != null && stop.getLng() != null);
    }

}
