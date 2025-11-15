package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalbrasiliant.services.impl.TripServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.TripMapper;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock
    private TripRepository tripRepo;

    @Mock
    private BusRepository busRepo;

    @Mock
    private RouteRepository routeRepo;

    @Spy
    private TripMapper tripMapper = Mappers.getMapper(TripMapper.class);

    @InjectMocks
    private TripServiceImpl service;

    @Test
    void shouldCreateAndReturnResponse() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var route = Route.builder().id(1L).build();

        var request = new TripCreateRequest(
                1L, // bus_id
                LocalDate.of(2025, 11, 15),
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(3)
        );

        when(busRepo.findById(1L)).thenReturn(Optional.of(bus));
        when(routeRepo.findById(1L)).thenReturn(Optional.of(route));

        when(tripRepo.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(10L);
            t.setStatus(TripStatus.SCHEDULED);
            return t;
        });

        // When
        var response = service.create(1L, request); // routeId as first parameter

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.route_id()).isEqualTo(1L);
        assertThat(response.bus_id()).isEqualTo(1L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2025, 11, 15));
        assertThat(response.status()).isEqualTo(TripStatus.SCHEDULED);
        assertThat(response.departureAt()).isNotNull();
        assertThat(response.arrivalETA()).isNotNull();

        verify(busRepo).findById(1L);
        verify(routeRepo).findById(1L);
        verify(tripRepo).save(any(Trip.class));
    }

    @Test
    void shouldGetTripById() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var route = Route.builder().id(1L).build();

        var trip = Trip.builder()
                .id(10L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 15))
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepo.findById(10L)).thenReturn(Optional.of(trip));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.bus_id()).isEqualTo(1L);
        assertThat(response.route_id()).isEqualTo(1L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2025, 11, 15));
        assertThat(response.status()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    void shouldUpdateTripViaPatch() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var newBus = Bus.builder().id(2L).build();
        var route = Route.builder().id(1L).build();

        var trip = Trip.builder()
                .id(10L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 15))
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        var updateRequest = new TripUpdateRequest(
                null, // route_id
                2L,   // bus_id
                LocalDate.of(2025, 11, 20),
                OffsetDateTime.now().plusDays(5),
                OffsetDateTime.now().plusDays(5).plusHours(4),
                TripStatus.BOARDING
        );

        when(tripRepo.findById(10L)).thenReturn(Optional.of(trip));
        when(busRepo.findById(2L)).thenReturn(Optional.of(newBus));
        when(tripRepo.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.bus_id()).isEqualTo(2L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2025, 11, 20));
        assertThat(response.status()).isEqualTo(TripStatus.BOARDING);
        verify(busRepo).findById(2L);
        verify(tripRepo).save(any(Trip.class));
    }

    @Test
    void shouldDeleteTrip() {
        // When
        service.delete(10L);

        // Then
        verify(tripRepo).deleteById(10L);
    }

    @Test
    void shouldGetAllTripsByRouteId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        var bus = Bus.builder().id(1L).build();
        var route = Route.builder().id(5L).build();

        var trip1 = Trip.builder()
                .id(1L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 15))
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        var trip2 = Trip.builder()
                .id(2L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 16))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        var page = new PageImpl<>(List.of(trip1, trip2));
        when(tripRepo.findAllByRoute_Id(5L, pageable)).thenReturn(page);

        // When
        var result = service.getAllByRouteId(5L, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(t -> t.route_id() == 5L);
    }

    @Test
    void shouldGetAllTripsByBusId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        var bus = Bus.builder().id(3L).build();
        var route = Route.builder().id(1L).build();

        var trip1 = Trip.builder()
                .id(1L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 15))
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        var trip2 = Trip.builder()
                .id(2L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 16))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(3))
                .status(TripStatus.DEPARTED)
                .build();

        var page = new PageImpl<>(List.of(trip1, trip2));
        when(tripRepo.findAllByBus_Id(3L, pageable)).thenReturn(page);

        // When
        var result = service.getAllByBusId(3L, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(t -> t.bus_id() == 3L);
    }

    @Test
    void shouldGetTripsByDepartureBetween() {
        // Given
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = OffsetDateTime.now().plusDays(7);
        Pageable pageable = PageRequest.of(0, 10);

        var bus = Bus.builder().id(1L).build();
        var route = Route.builder().id(1L).build();

        var trip1 = Trip.builder()
                .id(1L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 15))
                .departureAt(start.plusDays(2))
                .arrivalETA(start.plusDays(2).plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        var trip2 = Trip.builder()
                .id(2L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 18))
                .departureAt(start.plusDays(5))
                .arrivalETA(start.plusDays(5).plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        var page = new PageImpl<>(List.of(trip1, trip2));
        when(tripRepo.findAllByDepartureAtBetween(start, end, pageable)).thenReturn(page);

        // When
        var result = service.getByDepartureBetween(start, end, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(1).id()).isEqualTo(2L);
    }

    @Test
    void shouldGetTripsByArrivalBetween() {
        // Given
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = OffsetDateTime.now().plusDays(7);
        Pageable pageable = PageRequest.of(0, 10);

        var bus = Bus.builder().id(1L).build();
        var route = Route.builder().id(1L).build();

        var trip1 = Trip.builder()
                .id(1L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 15))
                .departureAt(start.plusDays(2))
                .arrivalETA(start.plusDays(2).plusHours(5))
                .status(TripStatus.SCHEDULED)
                .build();

        var trip2 = Trip.builder()
                .id(2L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 18))
                .departureAt(start.plusDays(5))
                .arrivalETA(start.plusDays(5).plusHours(4))
                .status(TripStatus.SCHEDULED)
                .build();

        var page = new PageImpl<>(List.of(trip1, trip2));
        when(tripRepo.findAllByArrivalETABetween(start, end, pageable)).thenReturn(page);

        // When
        var result = service.getByArrivalBetween(start, end, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(1).id()).isEqualTo(2L);
    }

    @Test
    void shouldGetTripsByStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        var bus = Bus.builder().id(1L).build();
        var route = Route.builder().id(1L).build();

        var trip1 = Trip.builder()
                .id(1L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 15))
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(3))
                .status(TripStatus.DEPARTED)
                .build();

        var trip2 = Trip.builder()
                .id(2L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 16))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(3))
                .status(TripStatus.DEPARTED)
                .build();

        var page = new PageImpl<>(List.of(trip1, trip2));
        when(tripRepo.findAllByStatus(TripStatus.DEPARTED, pageable)).thenReturn(page);

        // When
        var result = service.getByStatus(TripStatus.DEPARTED, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(t -> t.status() == TripStatus.DEPARTED);
    }

    @Test
    void shouldGetTripsByRouteIdAndStatus() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var route = Route.builder().id(5L).build();

        var trip1 = Trip.builder()
                .id(1L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 15))
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        var trip2 = Trip.builder()
                .id(2L)
                .bus(bus)
                .route(route)
                .date(LocalDate.of(2025, 11, 16))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepo.findAllByRoute_IdAndStatus(5L, TripStatus.SCHEDULED))
                .thenReturn(List.of(trip1, trip2));

        // When
        var result = service.getByRouteIdAndStatus(5L, TripStatus.SCHEDULED);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.route_id() == 5L);
        assertThat(result).allMatch(t -> t.status() == TripStatus.SCHEDULED);
    }

    @Test
    void shouldGetTripsByDate() {
        // Given
        LocalDate date = LocalDate.of(2025, 11, 15);
        Pageable pageable = PageRequest.of(0, 10);

        var bus = Bus.builder().id(1L).build();
        var route = Route.builder().id(1L).build();

        var trip1 = Trip.builder()
                .id(1L)
                .bus(bus)
                .route(route)
                .date(date)
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(3))
                .status(TripStatus.SCHEDULED)
                .build();

        var trip2 = Trip.builder()
                .id(2L)
                .bus(bus)
                .route(route)
                .date(date)
                .departureAt(OffsetDateTime.now().plusHours(6))
                .arrivalETA(OffsetDateTime.now().plusHours(9))
                .status(TripStatus.SCHEDULED)
                .build();

        var page = new PageImpl<>(List.of(trip1, trip2));
        when(tripRepo.findAllByDate(date, pageable)).thenReturn(page);

        // When
        var result = service.getByDate(date, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(t -> t.date().equals(date));
    }
}