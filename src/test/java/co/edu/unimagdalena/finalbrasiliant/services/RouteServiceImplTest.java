package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalbrasiliant.services.impl.RouteServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.RouteMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {

    @Mock
    private RouteRepository routeRepo;

    @Spy
    private RouteMapper routeMapper = Mappers.getMapper(RouteMapper.class);

    @InjectMocks
    private RouteServiceImpl service;

    @Test
    void shouldCreateAndReturnResponse() {
        // Given
        var request = new RouteCreateRequest(
                "SMR-CTG",
                "Santa Marta - Cartagena",
                "Santa Marta",
                "Cartagena",
                new BigDecimal("220.50"),
                240
        );

        when(routeRepo.save(any(Route.class))).thenAnswer(inv -> {
            Route r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("SMR-CTG");
        assertThat(response.routeName()).isEqualTo("Santa Marta - Cartagena");
        assertThat(response.origin()).isEqualTo("Santa Marta");
        assertThat(response.destination()).isEqualTo("Cartagena");
        assertThat(response.distanceKM()).isEqualByComparingTo(new BigDecimal("220.50"));
        assertThat(response.durationMin()).isEqualTo(240);

        verify(routeRepo).save(any(Route.class));
    }

    @Test
    void shouldGetRouteById() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("BOG-MED")
                .routeName("Bogotá - Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("415.00"))
                .durationMin(480)
                .build();

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));

        // When
        var response = service.get(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("BOG-MED");
        assertThat(response.routeName()).isEqualTo("Bogotá - Medellín");
        assertThat(response.origin()).isEqualTo("Bogotá");
        assertThat(response.destination()).isEqualTo("Medellín");
        assertThat(response.distanceKM()).isEqualByComparingTo(new BigDecimal("415.00"));
        assertThat(response.durationMin()).isEqualTo(480);
    }

    @Test
    void shouldUpdateRouteViaPatch() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("SMR-CTG")
                .routeName("Santa Marta - Cartagena")
                .origin("Santa Marta")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("220.50"))
                .durationMin(240)
                .build();

        var updateRequest = new RouteUpdateRequest(
                "SMR-CTG-V2",
                "Santa Marta - Cartagena Express",
                "Santa Marta",
                "Cartagena",
                new BigDecimal("225.00"),
                210
        );

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));
        when(routeRepo.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.code()).isEqualTo("SMR-CTG-V2");
        assertThat(response.routeName()).isEqualTo("Santa Marta - Cartagena Express");
        assertThat(response.distanceKM()).isEqualByComparingTo(new BigDecimal("225.00"));
        assertThat(response.durationMin()).isEqualTo(210);
        verify(routeRepo).save(any(Route.class));
    }

    @Test
    void shouldDeleteRoute() {
        // When
        service.delete(1L);

        // Then
        verify(routeRepo).deleteById(1L);
    }

    @Test
    void shouldGetRouteByCode() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("BAQ-CTG")
                .routeName("Barranquilla - Cartagena")
                .origin("Barranquilla")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("120.00"))
                .durationMin(120)
                .build();

        when(routeRepo.findByCode("BAQ-CTG")).thenReturn(Optional.of(route));

        // When
        var response = service.getByCode("BAQ-CTG");

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("BAQ-CTG");
        assertThat(response.routeName()).isEqualTo("Barranquilla - Cartagena");
    }

    @Test
    void shouldGetRouteByRouteName() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("CALI-POP")
                .routeName("Cali - Popayán")
                .origin("Cali")
                .destination("Popayán")
                .distanceKM(new BigDecimal("135.00"))
                .durationMin(150)
                .build();

        when(routeRepo.findByRouteName("Cali - Popayán")).thenReturn(Optional.of(route));

        // When
        var response = service.getByRouteName("Cali - Popayán");

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("CALI-POP");
        assertThat(response.routeName()).isEqualTo("Cali - Popayán");
    }

    @Test
    void shouldGetRoutesByOrigin() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("BOG-MED")
                .routeName("Bogotá - Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("415.00"))
                .durationMin(480)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("BOG-CALI")
                .routeName("Bogotá - Cali")
                .origin("Bogotá")
                .destination("Cali")
                .distanceKM(new BigDecimal("460.00"))
                .durationMin(540)
                .build();

        when(routeRepo.findAllByOriginIgnoreCase("Bogotá")).thenReturn(List.of(route1, route2));

        // When
        var result = service.getByOrigin("Bogotá");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.origin().equals("Bogotá"));
        assertThat(result.get(0).destination()).isEqualTo("Medellín");
        assertThat(result.get(1).destination()).isEqualTo("Cali");
    }

    @Test
    void shouldGetRoutesByDestination() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("SMR-CTG")
                .routeName("Santa Marta - Cartagena")
                .origin("Santa Marta")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("220.50"))
                .durationMin(240)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("BAQ-CTG")
                .routeName("Barranquilla - Cartagena")
                .origin("Barranquilla")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("120.00"))
                .durationMin(120)
                .build();

        when(routeRepo.findAllByDestinationIgnoreCase("Cartagena"))
                .thenReturn(List.of(route1, route2));

        // When
        var result = service.getByDestination("Cartagena");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.destination().equals("Cartagena"));
        assertThat(result.get(0).origin()).isEqualTo("Santa Marta");
        assertThat(result.get(1).origin()).isEqualTo("Barranquilla");
    }

    @Test
    void shouldGetRoutesByMinDurationGreaterThan() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("BOG-MED")
                .routeName("Bogotá - Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("415.00"))
                .durationMin(480)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("BOG-CALI")
                .routeName("Bogotá - Cali")
                .origin("Bogotá")
                .destination("Cali")
                .distanceKM(new BigDecimal("460.00"))
                .durationMin(540)
                .build();

        when(routeRepo.findAllByDurationMinGreaterThan(300)).thenReturn(List.of(route1, route2));

        // When
        var result = service.getByMinDurationGreaterThan(300);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.durationMin() > 300);
        assertThat(result.get(0).durationMin()).isEqualTo(480);
        assertThat(result.get(1).durationMin()).isEqualTo(540);
    }

    @Test
    void shouldGetRoutesByDurationBetween() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("SMR-CTG")
                .routeName("Santa Marta - Cartagena")
                .origin("Santa Marta")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("220.50"))
                .durationMin(240)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("CALI-POP")
                .routeName("Cali - Popayán")
                .origin("Cali")
                .destination("Popayán")
                .distanceKM(new BigDecimal("135.00"))
                .durationMin(150)
                .build();

        when(routeRepo.findAllByDurationMinBetween(100, 300))
                .thenReturn(List.of(route1, route2));

        // When
        var result = service.getByDurationBetween(100, 300);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.durationMin() >= 100 && r.durationMin() <= 300);
        assertThat(result.get(0).durationMin()).isEqualTo(240);
        assertThat(result.get(1).durationMin()).isEqualTo(150);
    }

    @Test
    void shouldGetRoutesByDistanceLessThan() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("BAQ-CTG")
                .routeName("Barranquilla - Cartagena")
                .origin("Barranquilla")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("120.00"))
                .durationMin(120)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("CALI-POP")
                .routeName("Cali - Popayán")
                .origin("Cali")
                .destination("Popayán")
                .distanceKM(new BigDecimal("135.00"))
                .durationMin(150)
                .build();

        when(routeRepo.findAllByDistanceKMLessThan(new BigDecimal("200.00")))
                .thenReturn(List.of(route1, route2));

        // When
        var result = service.getByDistanceLessThan(new BigDecimal("200.00"));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.distanceKM().compareTo(new BigDecimal("200.00")) < 0);
        assertThat(result.get(0).distanceKM()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(result.get(1).distanceKM()).isEqualByComparingTo(new BigDecimal("135.00"));
    }

    @Test
    void shouldGetRoutesByDistanceBetween() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("SMR-CTG")
                .routeName("Santa Marta - Cartagena")
                .origin("Santa Marta")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("220.50"))
                .durationMin(240)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("BOG-MED")
                .routeName("Bogotá - Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("415.00"))
                .durationMin(480)
                .build();

        when(routeRepo.findAllByDistanceKMBetween(new BigDecimal("200.00"), new BigDecimal("450.00")))
                .thenReturn(List.of(route1, route2));

        // When
        var result = service.getByDistanceBetween(new BigDecimal("200.00"), new BigDecimal("450.00"));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> 
            r.distanceKM().compareTo(new BigDecimal("200.00")) >= 0 && 
            r.distanceKM().compareTo(new BigDecimal("450.00")) <= 0
        );
        assertThat(result.get(0).distanceKM()).isEqualByComparingTo(new BigDecimal("220.50"));
        assertThat(result.get(1).distanceKM()).isEqualByComparingTo(new BigDecimal("415.00"));
    }

    @Test
    void shouldGetRoutesByOriginAndDestination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        var route1 = Route.builder()
                .id(1L)
                .code("BOG-MED-1")
                .routeName("Bogotá - Medellín Express")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("415.00"))
                .durationMin(480)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("BOG-MED-2")
                .routeName("Bogotá - Medellín Económico")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("425.00"))
                .durationMin(540)
                .build();

        var page = new PageImpl<>(List.of(route1, route2));
        when(routeRepo.findAllByOriginAndDestination("Bogotá", "Medellín", pageable))
                .thenReturn(page);

        // When
        var result = service.getByOriginAndDestination("Bogotá", "Medellín", pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(r -> 
            r.origin().equals("Bogotá") && r.destination().equals("Medellín")
        );
        assertThat(result.getContent().get(0).routeName()).isEqualTo("Bogotá - Medellín Express");
        assertThat(result.getContent().get(1).routeName()).isEqualTo("Bogotá - Medellín Económico");
    }
}