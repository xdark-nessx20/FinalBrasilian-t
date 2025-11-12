package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.StopDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class StopMapperTest {

    private final StopMapper mapper = Mappers.getMapper(StopMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        // Given
        var request = new StopCreateRequest(
                1L,
                "Terminal Bogotá",
                1,
                4.6097,
                -74.0817
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getName()).isEqualTo("Terminal Bogotá");
        assertThat(entity.getStopOrder()).isEqualTo(1);
        assertThat(entity.getLat()).isEqualTo(4.6097);
        assertThat(entity.getLng()).isEqualTo(-74.0817);

        // Campos ignorados
        assertThat(entity.getId()).isNull();
        assertThat(entity.getRoute()).isNull();
    }

    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var route = Route.builder()
                .id(1L)
                .origin("Bogotá")
                .destination("Medellín")
                .build();

        var stop = Stop.builder()
                .id(10L)
                .route(route)
                .name("Terminal Medellín")
                .stopOrder(3)
                .lat(6.2442)
                .lng(-75.5812)
                .build();

        // When
        var response = mapper.toResponse(stop);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(1L); // Mapped from route.id
        assertThat(response.name()).isEqualTo("Terminal Medellín");
        assertThat(response.stopOrder()).isEqualTo(3);
        assertThat(response.lat()).isEqualTo(6.2442);
        assertThat(response.lng()).isEqualTo(-75.5812);
    }

    @Test
    void patch_shouldUpdateAllFields() {
        // Given
        var stop = Stop.builder()
                .id(10L)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();

        var updateRequest = new StopUpdateRequest(
                "Terminal Norte Bogotá",
                2,
                4.7000,
                -74.1000
        );

        // When
        mapper.patch(stop, updateRequest);

        // Then
        assertThat(stop.getName()).isEqualTo("Terminal Norte Bogotá");
        assertThat(stop.getStopOrder()).isEqualTo(2);
        assertThat(stop.getLat()).isEqualTo(4.7000);
        assertThat(stop.getLng()).isEqualTo(-74.1000);
        assertThat(stop.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFields() {
        // Given
        var stop = Stop.builder()
                .id(10L)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();

        var updateRequest = new StopUpdateRequest(null, null, null, null);

        // When
        mapper.patch(stop, updateRequest);

        // Then
        assertThat(stop.getName()).isEqualTo("Terminal Bogotá"); // No cambió
        assertThat(stop.getStopOrder()).isEqualTo(1); // No cambió
        assertThat(stop.getLat()).isEqualTo(4.6097); // No cambió
        assertThat(stop.getLng()).isEqualTo(-74.0817); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyCoordinates() {
        // Given
        var stop = Stop.builder()
                .id(10L)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();

        var updateRequest = new StopUpdateRequest(null, null, 5.0000, -75.0000);

        // When
        mapper.patch(stop, updateRequest);

        // Then
        assertThat(stop.getLat()).isEqualTo(5.0000); // Cambió
        assertThat(stop.getLng()).isEqualTo(-75.0000); // Cambió
        assertThat(stop.getName()).isEqualTo("Terminal Bogotá"); // No cambió
        assertThat(stop.getStopOrder()).isEqualTo(1); // No cambió
    }


    @Test
    void patch_shouldNotModifyIdOrRoute() {
        // Given
        var route = Route.builder().id(1L).build();

        var stop = Stop.builder()
                .id(10L)
                .route(route)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();

        var updateRequest = new StopUpdateRequest(
                "Nuevo Nombre",
                99,
                10.0000,
                -80.0000
        );

        // When
        mapper.patch(stop, updateRequest);

        // Then
        assertThat(stop.getId()).isEqualTo(10L); // No cambió
        assertThat(stop.getRoute()).isEqualTo(route); // No cambió
    }


    @Test
    void toEntity_shouldMapWithZeroCoordinates() {
        // Given - Ecuador pasa por 0° latitud
        var request = new StopCreateRequest(
                1L,
                "Quito",
                1,
                0.0,
                -78.0
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getLat()).isEqualTo(0.0);
        assertThat(entity.getLng()).isEqualTo(-78.0);
    }
}
