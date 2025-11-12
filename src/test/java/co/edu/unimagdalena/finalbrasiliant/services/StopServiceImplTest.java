package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.StopDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.services.impl.StopServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.StopMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class StopServiceImplTest {

    @Mock
    private StopRepository stopRepo;

    @Mock
    private RouteRepository routeRepo;

    @Spy
    private StopMapper mapper = Mappers.getMapper(StopMapper.class);

    @InjectMocks
    private StopServiceImpl service;

    @Test
    void shouldCreateStopSuccessfully() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();

        var request = new StopCreateRequest(1L, "Terminal Bogotá", 1, 4.6097, -74.0817);

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepo.existsByRouteIdAndNameIgnoreCase(1L, "Terminal Bogotá")).thenReturn(false);
        when(stopRepo.save(any(Stop.class))).thenAnswer(inv -> {
            Stop s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Terminal Bogotá");
        assertThat(response.stopOrder()).isEqualTo(1);
        assertThat(response.lat()).isEqualTo(4.6097);
        assertThat(response.lng()).isEqualTo(-74.0817);

        verify(stopRepo).save(any(Stop.class));
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenStopNameExists() {
        // Given
        var route = Route.builder().id(1L).build();
        var request = new StopCreateRequest(1L, "Terminal Bogotá", 1, null, null);

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepo.existsByRouteIdAndNameIgnoreCase(1L, "Terminal Bogotá")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Stop 'Terminal Bogotá' already exists in route 1");

        verify(stopRepo, never()).save(any());
    }

    @Test
    void shouldGetStopById() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").build();

        var stop = Stop.builder()
                .id(10L)
                .route(route)
                .name("Terminal Medellín")
                .stopOrder(3)
                .lat(6.2442)
                .lng(-75.5812)
                .build();

        when(stopRepo.findByIdWithRoute(10L)).thenReturn(Optional.of(stop));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Terminal Medellín");
    }

    @Test
    void shouldUpdateStop() {
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

        var updateRequest = new StopUpdateRequest("Terminal Norte", 2, 4.7000, -74.1000);

        when(stopRepo.findById(10L)).thenReturn(Optional.of(stop));
        when(stopRepo.existsByRouteIdAndNameIgnoreCase(1L, "Terminal Norte")).thenReturn(false);
        when(stopRepo.save(any(Stop.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.name()).isEqualTo("Terminal Norte");
        assertThat(response.stopOrder()).isEqualTo(2);
        assertThat(response.lat()).isEqualTo(4.7000);
        assertThat(response.lng()).isEqualTo(-74.1000);

        verify(stopRepo).save(any(Stop.class));
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenUpdateWithExistingName() {
        // Given
        var route = Route.builder().id(1L).build();

        var stop = Stop.builder()
                .id(10L)
                .route(route)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .build();

        var updateRequest = new StopUpdateRequest("Terminal Medellín", null, null, null);

        when(stopRepo.findById(10L)).thenReturn(Optional.of(stop));
        when(stopRepo.existsByRouteIdAndNameIgnoreCase(1L, "Terminal Medellín")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.update(10L, updateRequest))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Another stop with name 'Terminal Medellín' already exists");

        verify(stopRepo, never()).save(any());
    }

    @Test
    void shouldDeleteStop() {
        // Given
        when(stopRepo.existsById(10L)).thenReturn(true);

        // When
        service.delete(10L);

        // Then
        verify(stopRepo).deleteById(10L);
    }

    @Test
    void shouldListStopsByRoute() {
        // Given
        var route = Route.builder().id(1L).build();

        var stop1 = Stop.builder()
                .id(1L)
                .route(route)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .build();

        var stop2 = Stop.builder()
                .id(2L)
                .route(route)
                .name("Terminal Medellín")
                .stopOrder(2)
                .build();

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepo.findByRoute_IdOrderByStopOrderAsc(1L)).thenReturn(List.of(stop1, stop2));

        // When
        var result = service.listByRoute(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).stopOrder()).isEqualTo(1);
        assertThat(result.get(1).stopOrder()).isEqualTo(2);
    }

    @Test
    void shouldGetFirstStopByRoute() {
        // Given
        var route = Route.builder().id(1L).build();

        var firstStop = Stop.builder()
                .id(1L)
                .route(route)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .build();

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepo.findFirstByRoute_IdOrderByStopOrderAsc(1L)).thenReturn(Optional.of(firstStop));

        // When
        var response = service.getFirstStop(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Terminal Bogotá");
        assertThat(response.stopOrder()).isEqualTo(1);
    }
}
