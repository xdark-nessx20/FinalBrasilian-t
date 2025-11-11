package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RouteMapperTest {

    private final RouteMapper mapper = Mappers.getMapper(RouteMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        // Given
        var request = new RouteCreateRequest(
                "R001",
                "Santa Marta - Barranquilla",
                "Santa Marta",
                "Barranquilla",
                new BigDecimal("95.50"),
                120
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getCode()).isEqualTo("R001");
        assertThat(entity.getRouteName()).isEqualTo("Santa Marta - Barranquilla");
        assertThat(entity.getOrigin()).isEqualTo("Santa Marta");
        assertThat(entity.getDestination()).isEqualTo("Barranquilla");
        assertThat(entity.getDistanceKM()).isEqualByComparingTo(new BigDecimal("95.50"));
        assertThat(entity.getDurationMin()).isEqualTo(120);
        assertThat(entity.getId()).isNull(); // Ignored by mapper
    }

    @Test
    void toEntity_shouldMapCreateRequestWithDifferentRoute() {
        // Given
        var request = new RouteCreateRequest(
                "R002",
                "Bogotá - Medellín",
                "Bogotá",
                "Medellín",
                new BigDecimal("415.00"),
                540
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getCode()).isEqualTo("R002");
        assertThat(entity.getRouteName()).isEqualTo("Bogotá - Medellín");
        assertThat(entity.getOrigin()).isEqualTo("Bogotá");
        assertThat(entity.getDestination()).isEqualTo("Medellín");
        assertThat(entity.getDistanceKM()).isEqualByComparingTo(new BigDecimal("415.00"));
        assertThat(entity.getDurationMin()).isEqualTo(540);
    }

    @Test
    void toEntity_shouldMapCreateRequestWithShortRoute() {
        // Given
        var request = new RouteCreateRequest(
                "R003",
                "Cartagena - Barranquilla",
                "Cartagena",
                "Barranquilla",
                new BigDecimal("120.75"),
                90
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getCode()).isEqualTo("R003");
        assertThat(entity.getDistanceKM()).isEqualByComparingTo(new BigDecimal("120.75"));
        assertThat(entity.getDurationMin()).isEqualTo(90);
    }

    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Cali - Popayán")
                .origin("Cali")
                .destination("Popayán")
                .distanceKM(new BigDecimal("135.25"))
                .durationMin(180)
                .build();

        // When
        var response = mapper.toResponse(route);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo("R001");
        assertThat(response.routeName()).isEqualTo("Cali - Popayán");
        assertThat(response.origin()).isEqualTo("Cali");
        assertThat(response.destination()).isEqualTo("Popayán");
        assertThat(response.distanceKM()).isEqualByComparingTo(new BigDecimal("135.25"));
        assertThat(response.durationMin()).isEqualTo(180);
    }

    @Test
    void toResponse_shouldMapEntityWithLongRoute() {
        // Given
        var route = Route.builder()
                .id(20L)
                .code("R010")
                .routeName("Bogotá - Cúcuta")
                .origin("Bogotá")
                .destination("Cúcuta")
                .distanceKM(new BigDecimal("550.00"))
                .durationMin(660)
                .build();

        // When
        var response = mapper.toResponse(route);

        // Then
        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.code()).isEqualTo("R010");
        assertThat(response.routeName()).isEqualTo("Bogotá - Cúcuta");
        assertThat(response.distanceKM()).isEqualByComparingTo(new BigDecimal("550.00"));
        assertThat(response.durationMin()).isEqualTo(660);
    }

    @Test
    void toResponse_shouldMapEntityWithDecimalDistance() {
        // Given
        var route = Route.builder()
                .id(30L)
                .code("R015")
                .routeName("Pereira - Armenia")
                .origin("Pereira")
                .destination("Armenia")
                .distanceKM(new BigDecimal("45.80"))
                .durationMin(60)
                .build();

        // When
        var response = mapper.toResponse(route);

        // Then
        assertThat(response.distanceKM()).isEqualByComparingTo(new BigDecimal("45.80"));
        assertThat(response.durationMin()).isEqualTo(60);
    }

    @Test
    void patch_shouldUpdateAllFields() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Original")
                .origin("Ciudad A")
                .destination("Ciudad B")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(
                "R999",
                "Ruta Actualizada",
                "Ciudad C",
                "Ciudad D",
                new BigDecimal("250.50"),
                300
        );

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getCode()).isEqualTo("R999");
        assertThat(route.getRouteName()).isEqualTo("Ruta Actualizada");
        assertThat(route.getOrigin()).isEqualTo("Ciudad C");
        assertThat(route.getDestination()).isEqualTo("Ciudad D");
        assertThat(route.getDistanceKM()).isEqualByComparingTo(new BigDecimal("250.50"));
        assertThat(route.getDurationMin()).isEqualTo(300);
        assertThat(route.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFields() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Original")
                .origin("Santa Marta")
                .destination("Barranquilla")
                .distanceKM(new BigDecimal("95.50"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(null, null, null, null, null, null);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getCode()).isEqualTo("R001"); // No cambió
        assertThat(route.getRouteName()).isEqualTo("Ruta Original"); // No cambió
        assertThat(route.getOrigin()).isEqualTo("Santa Marta"); // No cambió
        assertThat(route.getDestination()).isEqualTo("Barranquilla"); // No cambió
        assertThat(route.getDistanceKM()).isEqualByComparingTo(new BigDecimal("95.50")); // No cambió
        assertThat(route.getDurationMin()).isEqualTo(120); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyCode() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Test")
                .origin("Origen")
                .destination("Destino")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest("R888", null, null, null, null, null);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getCode()).isEqualTo("R888"); // Cambió
        assertThat(route.getRouteName()).isEqualTo("Ruta Test"); // No cambió
        assertThat(route.getOrigin()).isEqualTo("Origen"); // No cambió
        assertThat(route.getDestination()).isEqualTo("Destino"); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyRouteName() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Vieja")
                .origin("Origen")
                .destination("Destino")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(null, "Ruta Nueva", null, null, null, null);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getRouteName()).isEqualTo("Ruta Nueva"); // Cambió
        assertThat(route.getCode()).isEqualTo("R001"); // No cambió
        assertThat(route.getOrigin()).isEqualTo("Origen"); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyOrigin() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Test")
                .origin("Origen Original")
                .destination("Destino")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(null, null, "Nuevo Origen", null, null, null);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getOrigin()).isEqualTo("Nuevo Origen"); // Cambió
        assertThat(route.getDestination()).isEqualTo("Destino"); // No cambió
        assertThat(route.getCode()).isEqualTo("R001"); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyDestination() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Test")
                .origin("Origen")
                .destination("Destino Original")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(null, null, null, "Nuevo Destino", null, null);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getDestination()).isEqualTo("Nuevo Destino"); // Cambió
        assertThat(route.getOrigin()).isEqualTo("Origen"); // No cambió
        assertThat(route.getCode()).isEqualTo("R001"); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyDistance() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Test")
                .origin("Origen")
                .destination("Destino")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(null, null, null, null, new BigDecimal("175.75"), null);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getDistanceKM()).isEqualByComparingTo(new BigDecimal("175.75")); // Cambió
        assertThat(route.getDurationMin()).isEqualTo(120); // No cambió
        assertThat(route.getCode()).isEqualTo("R001"); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyDuration() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Test")
                .origin("Origen")
                .destination("Destino")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(null, null, null, null, null, 240);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getDurationMin()).isEqualTo(240); // Cambió
        assertThat(route.getDistanceKM()).isEqualByComparingTo(new BigDecimal("100.00")); // No cambió
        assertThat(route.getCode()).isEqualTo("R001"); // No cambió
    }

    @Test
    void patch_shouldUpdateDistanceAndDuration() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Test")
                .origin("Origen")
                .destination("Destino")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(null, null, null, null, new BigDecimal("200.50"), 300);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getDistanceKM()).isEqualByComparingTo(new BigDecimal("200.50")); // Cambió
        assertThat(route.getDurationMin()).isEqualTo(300); // Cambió
        assertThat(route.getCode()).isEqualTo("R001"); // No cambió
        assertThat(route.getRouteName()).isEqualTo("Ruta Test"); // No cambió
    }

    @Test
    void patch_shouldUpdateOriginAndDestination() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Test")
                .origin("Origen A")
                .destination("Destino A")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(null, null, "Origen B", "Destino B", null, null);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getOrigin()).isEqualTo("Origen B"); // Cambió
        assertThat(route.getDestination()).isEqualTo("Destino B"); // Cambió
        assertThat(route.getCode()).isEqualTo("R001"); // No cambió
        assertThat(route.getDistanceKM()).isEqualByComparingTo(new BigDecimal("100.00")); // No cambió
    }

    @Test
    void patch_shouldNotModifyId() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Original")
                .origin("Origen")
                .destination("Destino")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest(
                "R999",
                "Ruta Actualizada",
                "Nuevo Origen",
                "Nuevo Destino",
                new BigDecimal("250.00"),
                300
        );

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldUpdateCodeAndRouteName() {
        // Given
        var route = Route.builder()
                .id(10L)
                .code("R001")
                .routeName("Ruta Vieja")
                .origin("Origen")
                .destination("Destino")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        var updateRequest = new RouteUpdateRequest("R555", "Ruta Renovada", null, null, null, null);

        // When
        mapper.patch(route, updateRequest);

        // Then
        assertThat(route.getCode()).isEqualTo("R555"); // Cambió
        assertThat(route.getRouteName()).isEqualTo("Ruta Renovada"); // Cambió
        assertThat(route.getOrigin()).isEqualTo("Origen"); // No cambió
        assertThat(route.getDestination()).isEqualTo("Destino"); // No cambió
    }
}