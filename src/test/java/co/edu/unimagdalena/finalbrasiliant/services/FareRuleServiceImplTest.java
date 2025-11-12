package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.FareRuleDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.FareRule;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.FareRuleRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.services.impl.FareRuleServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.FareRuleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class FareRuleServiceImplTest {

    @Mock
    private FareRuleRepository fareRuleRepo;

    @Mock
    private RouteRepository routeRepo;

    @Mock
    private StopRepository stopRepo;

    @Spy
    private FareRuleMapper mapper = Mappers.getMapper(FareRuleMapper.class);

    @InjectMocks
    private FareRuleServiceImpl service;

    @Test
    void shouldCreateFareRuleSuccessfully() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var fromStop = Stop.builder().id(2L).route(route).stopOrder(1).build();
        var toStop = Stop.builder().id(3L).route(route).stopOrder(3).build();

        var request = new FareRuleCreateRequest(
                1L, 2L, 3L,
                new BigDecimal("50000.00"),
                List.of("STUDENT:10"),
                DynamicPricing.ON
        );

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepo.findById(2L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepo.existsByRouteIdAndFromStopIdAndToStopId(1L, 2L, 3L)).thenReturn(false);
        when(fareRuleRepo.save(any(FareRule.class))).thenAnswer(inv -> {
            FareRule f = inv.getArgument(0);
            f.setId(10L);
            return f;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.basePrice()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(response.dynamicPricing()).isEqualTo(DynamicPricing.ON);

        verify(fareRuleRepo).save(any(FareRule.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFromStopNotInRoute() {
        // Given
        var route = Route.builder().id(1L).build();
        var otherRoute = Route.builder().id(99L).build();
        var fromStop = Stop.builder().id(2L).route(otherRoute).build();

        var request = new FareRuleCreateRequest(
                1L, 2L, 3L,
                new BigDecimal("50000.00"),
                List.of(),
                DynamicPricing.ON
        );

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepo.findById(2L)).thenReturn(Optional.of(fromStop));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Origin stop 2 not exist on route 1");

        verify(fareRuleRepo, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFromStopAfterToStop() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(2L).route(route).stopOrder(3).build();
        var toStop = Stop.builder().id(3L).route(route).stopOrder(1).build();

        var request = new FareRuleCreateRequest(
                1L, 2L, 3L,
                new BigDecimal("50000.00"),
                List.of(),
                DynamicPricing.ON
        );

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepo.findById(2L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(toStop));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Origin stop must come before destination stop");

        verify(fareRuleRepo, never()).save(any());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenFareRuleExists() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(2L).route(route).stopOrder(1).build();
        var toStop = Stop.builder().id(3L).route(route).stopOrder(3).build();

        var request = new FareRuleCreateRequest(
                1L, 2L, 3L,
                new BigDecimal("50000.00"),
                List.of(),
                DynamicPricing.ON
        );

        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepo.findById(2L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepo.existsByRouteIdAndFromStopIdAndToStopId(1L, 2L, 3L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Already exist a fare for route 1");

        verify(fareRuleRepo, never()).save(any());
    }

    @Test
    void shouldGetFareRuleById() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").build();
        var fromStop = Stop.builder().id(2L).name("Terminal Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(3L).name("Terminal Medellín").stopOrder(3).build();

        var fareRule = FareRule.builder()
                .id(10L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000.00"))
                .dynamicPricing(DynamicPricing.ON)
                .build();

        when(fareRuleRepo.findByIdWithAllDetails(10L)).thenReturn(Optional.of(fareRule));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.route().routeName()).isEqualTo("Bogotá");
    }

    @Test
    void shouldUpdateFareRule() {
        // Given
        var fareRule = FareRule.builder()
                .id(10L)
                .basePrice(new BigDecimal("50000.00"))
                .discounts(List.of("STUDENT:10"))
                .dynamicPricing(DynamicPricing.OFF)
                .build();

        var updateRequest = new FareRuleUpdateRequest(
                new BigDecimal("60000.00"),
                List.of("SENIOR:15"),
                DynamicPricing.ON
        );

        when(fareRuleRepo.findById(10L)).thenReturn(Optional.of(fareRule));
        when(fareRuleRepo.save(any(FareRule.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.basePrice()).isEqualByComparingTo(new BigDecimal("60000.00"));
        assertThat(response.dynamicPricing()).isEqualTo(DynamicPricing.ON);

        verify(fareRuleRepo).save(any(FareRule.class));
    }

    @Test
    void shouldDeleteFareRule() {
        // Given
        when(fareRuleRepo.existsById(10L)).thenReturn(true);

        // When
        service.delete(10L);

        // Then
        verify(fareRuleRepo).deleteById(10L);
    }

    @Test
    void shouldListFareRulesByDynamicPricing() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").build();
        var fromStop = Stop.builder().id(2L).name("Stop1").stopOrder(1).build();
        var toStop = Stop.builder().id(3L).name("Stop2").stopOrder(2).build();

        var fareRule1 = FareRule.builder()
                .id(1L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000.00"))
                .dynamicPricing(DynamicPricing.ON)
                .build();

        when(fareRuleRepo.findByDynamicPricing(DynamicPricing.ON))
                .thenReturn(List.of(fareRule1));

        // When
        var result = service.listByDynamicPricing(DynamicPricing.ON);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).dynamicPricing()).isEqualTo(DynamicPricing.ON);
    }
}
