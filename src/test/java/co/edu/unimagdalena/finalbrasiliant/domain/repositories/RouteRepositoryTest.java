package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class RouteRepositoryTest extends AbstractRepository {

    @Autowired
    private RouteRepository routeRepository;

    private Route route1;
    private Route route2;
    private Route route3;
    private Route route4;
    private Route route5;

    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();

        route1 = createRoute(
                "RT001",
                "Ruta Bogotá - Medellín",
                "Bogotá",
                "Medellín",
                new BigDecimal("415.50"),
                360
        );

        route2 = createRoute(
                "RT002",
                "Ruta Bogotá - Cali",
                "Bogotá",
                "Cali",
                new BigDecimal("450.75"),
                480
        );

        route3 = createRoute(
                "RT003",
                "Ruta Medellín - Cartagena",
                "Medellín",
                "Cartagena",
                new BigDecimal("630.25"),
                720
        );

        route4 = createRoute(
                "RT004",
                "Ruta Santa Marta - Barranquilla",
                "Santa Marta",
                "Barranquilla",
                new BigDecimal("95.30"),
                120
        );

        route5 = createRoute(
                "RT005",
                "Ruta Bogotá - Bucaramanga",
                "Bogotá",
                "Bucaramanga",
                new BigDecimal("385.00"),
                420
        );
    }

    private Route createRoute(String code, String routeName, String origin,
                            String destination, BigDecimal distanceKM, Integer durationMin) {
        return routeRepository.save(Route.builder()
                .code(code)
                .routeName(routeName)
                .origin(origin)
                .destination(destination)
                .distanceKM(distanceKM)
                .durationMin(durationMin)
                .build());
    }

    @Test
    void shouldFindRouteByCode() {
        Optional<Route> result = routeRepository.findByCode("RT001");

        assertThat(result).isPresent()
                .hasValueSatisfying(route -> {
                    assertThat(route.getId()).isEqualTo(route1.getId());
                    assertThat(route.getCode()).isEqualTo("RT001");
                    assertThat(route.getRouteName()).isEqualTo("Ruta Bogotá - Medellín");
                    assertThat(route.getOrigin()).isEqualTo("Bogotá");
                    assertThat(route.getDestination()).isEqualTo("Medellín");
                });
    }

    @Test
    void shouldFindAllRoutesByDestinationIgnoreCase() {
        List<Route> result = routeRepository.findAllByDestinationIgnoreCase("medellín");

        assertThat(result)
                .hasSize(1)
                .extracting(Route::getId)
                .containsExactly(route1.getId());
    }

    @Test
    void shouldFindMultipleRoutesByDestination() {
        List<Route> result = routeRepository.findAllByDestinationIgnoreCase("CARTAGENA");

        assertThat(result)
                .hasSize(1)
                .extracting(Route::getCode)
                .containsExactly("RT003");
    }

    @Test
    void shouldFindAllRoutesByOriginIgnoreCase() {
        List<Route> result = routeRepository.findAllByOriginIgnoreCase("BOGOTÁ");

        assertThat(result)
                .hasSize(3)
                .extracting(Route::getId)
                .containsExactlyInAnyOrder(route1.getId(), route2.getId(), route5.getId());
    }

    @Test
    void shouldFindRouteByRouteName() {
        Optional<Route> result = routeRepository.findByRouteName("Ruta Santa Marta - Barranquilla");

        assertThat(result).isPresent()
                .hasValueSatisfying(route -> {
                    assertThat(route.getId()).isEqualTo(route4.getId());
                    assertThat(route.getCode()).isEqualTo("RT004");
                    assertThat(route.getOrigin()).isEqualTo("Santa Marta");
                    assertThat(route.getDestination()).isEqualTo("Barranquilla");
                });
    }

    @Test
    void shouldFindAllRoutesByDurationMinGreaterThan() {
        List<Route> result = routeRepository.findAllByDurationMinGreaterThan(400);

        assertThat(result)
                .hasSize(3)
                .extracting(Route::getId)
                .containsExactlyInAnyOrder(route2.getId(), route3.getId(), route5.getId());

        assertThat(result)
                .allSatisfy(route -> assertThat(route.getDurationMin()).isGreaterThan(400));
    }

    @Test
    void shouldFindAllRoutesByDurationMinBetween() {
        List<Route> result = routeRepository.findAllByDurationMinBetween(300, 500);

        assertThat(result)
                .hasSize(3)
                .extracting(Route::getId)
                .containsExactlyInAnyOrder(route1.getId(), route2.getId(), route5.getId());

        assertThat(result)
                .allSatisfy(route -> {
                    assertThat(route.getDurationMin()).isGreaterThanOrEqualTo(300);
                    assertThat(route.getDurationMin()).isLessThanOrEqualTo(500);
                });
    }

    @Test
    void shouldFindAllRoutesByDistanceKMLessThan() {
        BigDecimal maxDistance = new BigDecimal("400.00");

        List<Route> result = routeRepository.findAllByDistanceKMLessThan(maxDistance);

        assertThat(result)
                .hasSize(2)
                .extracting(Route::getId)
                .containsExactlyInAnyOrder(route4.getId(), route5.getId());

        assertThat(result)
                .allSatisfy(route -> 
                    assertThat(route.getDistanceKM()).isLessThan(maxDistance));
    }

    @Test
    void shouldFindAllRoutesByDistanceKMBetween() {
        BigDecimal minDistance = new BigDecimal("400.00");
        BigDecimal maxDistance = new BigDecimal("500.00");

        List<Route> result = routeRepository.findAllByDistanceKMBetween(minDistance, maxDistance);

        assertThat(result)
                .hasSize(2)
                .extracting(Route::getId)
                .containsExactlyInAnyOrder(route1.getId(), route2.getId());

        assertThat(result)
                .allSatisfy(route -> {
                    assertThat(route.getDistanceKM()).isGreaterThanOrEqualTo(minDistance);
                    assertThat(route.getDistanceKM()).isLessThanOrEqualTo(maxDistance);
                });
    }

    @Test
    void shouldFindAllRoutesByOriginAndDestination() {
        Page<Route> result = routeRepository.findAllByOriginAndDestination(
                "Bogotá", "Medellín", PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Route::getId)
                .containsExactly(route1.getId());

        assertThat(result.getContent().get(0))
                .satisfies(route -> {
                    assertThat(route.getOrigin()).isEqualTo("Bogotá");
                    assertThat(route.getDestination()).isEqualTo("Medellín");
                    assertThat(route.getCode()).isEqualTo("RT001");
                });
    }

    @Test
    void shouldFindMultipleRoutesFromSameOrigin() {
        Page<Route> result = routeRepository.findAllByOriginAndDestination(
                "Bogotá", "Cali", PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Route::getCode)
                .containsExactly("RT002");
    }
}