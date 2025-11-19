package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TripMapperTest {

    private final TripMapper mapper = Mappers.getMapper(TripMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        // Given
        var departureTime = OffsetDateTime.now().plusHours(2);
        var arrivalTime = OffsetDateTime.now().plusHours(6);
        var request = new TripCreateRequest(
                2L,
                LocalDate.of(2025, 12, 25),
                departureTime,
                arrivalTime
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getRoute()).isNull();
        assertThat(entity.getBus()).isNull();
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2025, 12, 25));
        assertThat(entity.getDepartureAt()).isEqualTo(departureTime);
        assertThat(entity.getArrivalETA()).isEqualTo(arrivalTime);
        assertThat(entity.getId()).isNull(); // Ignored by mapper
    }

    @Test
    void toEntity_shouldMapCreateRequestWithDifferentDate() {
        // Given
        var departureTime = OffsetDateTime.now().plusDays(1);
        var arrivalTime = OffsetDateTime.now().plusDays(1).plusHours(4);
        var request = new TripCreateRequest(
                10L,
                LocalDate.of(2025, 11, 15),
                departureTime,
                arrivalTime
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getRoute()).isNull();
        assertThat(entity.getBus()).isNull();
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2025, 11, 15));
        assertThat(entity.getDepartureAt()).isEqualTo(departureTime);
        assertThat(entity.getArrivalETA()).isEqualTo(arrivalTime);
    }

    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var departureTime = OffsetDateTime.now().plusHours(3);
        var arrivalTime = OffsetDateTime.now().plusHours(7);
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).build();
        
        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 31))
                .departureAt(departureTime)
                .arrivalETA(arrivalTime)
                .status(TripStatus.SCHEDULED)
                .build();

        // When
        var response = mapper.toResponse(trip);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.route_id()).isEqualTo(1L);
        assertThat(response.bus_id()).isEqualTo(2L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(response.departureAt()).isEqualTo(departureTime);
        assertThat(response.arrivalETA()).isEqualTo(arrivalTime);
        assertThat(response.status()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    void toResponse_shouldMapEntityWithDepartedStatus() {
        // Given
        var departureTime = OffsetDateTime.now();
        var arrivalTime = OffsetDateTime.now().plusHours(5);
        var route = Route.builder().id(3L).build();
        var bus = Bus.builder().id(4L).build();
        
        var trip = Trip.builder()
                .id(20L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureAt(departureTime)
                .arrivalETA(arrivalTime)
                .status(TripStatus.DEPARTED)
                .build();

        // When
        var response = mapper.toResponse(trip);

        // Then
        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.status()).isEqualTo(TripStatus.DEPARTED);
        assertThat(response.route_id()).isEqualTo(3L);
        assertThat(response.bus_id()).isEqualTo(4L);
    }

    @Test
    void toResponse_shouldMapEntityWithArrivedStatus() {
        // Given
        var departureTime = OffsetDateTime.now().minusHours(5);
        var arrivalTime = OffsetDateTime.now().minusHours(1);
        var route = Route.builder().id(7L).build();
        var bus = Bus.builder().id(8L).build();
        
        var trip = Trip.builder()
                .id(30L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureAt(departureTime)
                .arrivalETA(arrivalTime)
                .status(TripStatus.ARRIVED)
                .build();

        // When
        var response = mapper.toResponse(trip);

        // Then
        assertThat(response.status()).isEqualTo(TripStatus.ARRIVED);
        assertThat(response.id()).isEqualTo(30L);
    }

    @Test
    void patch_shouldUpdateAllFields() {
        // Given
        var oldDeparture = OffsetDateTime.now();
        var oldArrival = OffsetDateTime.now().plusHours(4);
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).build();
        
        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 11, 11))
                .departureAt(oldDeparture)
                .arrivalETA(oldArrival)
                .status(TripStatus.SCHEDULED)
                .build();

        var newDeparture = OffsetDateTime.now().plusDays(1);
        var newArrival = OffsetDateTime.now().plusDays(1).plusHours(5);
        var updateRequest = new TripUpdateRequest(
                3L,
                4L,
                LocalDate.of(2025, 12, 1),
                newDeparture,
                newArrival,
                TripStatus.BOARDING
        );

        // When
        mapper.patch(trip, updateRequest);

        // Then
        assertThat(trip.getRoute().getId()).isEqualTo(3L);
        assertThat(trip.getBus().getId()).isEqualTo(4L);
        assertThat(trip.getDate()).isEqualTo(LocalDate.of(2025, 12, 1));
        assertThat(trip.getDepartureAt()).isEqualTo(newDeparture);
        assertThat(trip.getArrivalETA()).isEqualTo(newArrival);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.BOARDING);
        assertThat(trip.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFields() {
        // Given
        var departureTime = OffsetDateTime.now();
        var arrivalTime = OffsetDateTime.now().plusHours(3);
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).build();
        
        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 11, 20))
                .departureAt(departureTime)
                .arrivalETA(arrivalTime)
                .status(TripStatus.SCHEDULED)
                .build();

        var updateRequest = new TripUpdateRequest(null, null, null, null, null, null);

        // When
        mapper.patch(trip, updateRequest);

        // Then
        assertThat(trip.getRoute().getId()).isEqualTo(1L); // No cambió
        assertThat(trip.getBus().getId()).isEqualTo(2L); // No cambió
        assertThat(trip.getDate()).isEqualTo(LocalDate.of(2025, 11, 20)); // No cambió
        assertThat(trip.getDepartureAt()).isEqualTo(departureTime); // No cambió
        assertThat(trip.getArrivalETA()).isEqualTo(arrivalTime); // No cambió
        assertThat(trip.getStatus()).isEqualTo(TripStatus.SCHEDULED); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyStatus() {
        // Given
        var departureTime = OffsetDateTime.now();
        var arrivalTime = OffsetDateTime.now().plusHours(4);
        var route = Route.builder().id(5L).build();
        var bus = Bus.builder().id(6L).build();
        
        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureAt(departureTime)
                .arrivalETA(arrivalTime)
                .status(TripStatus.SCHEDULED)
                .build();

        var updateRequest = new TripUpdateRequest(null, null, null, null, null, TripStatus.DEPARTED);

        // When
        mapper.patch(trip, updateRequest);

        // Then
        assertThat(trip.getStatus()).isEqualTo(TripStatus.DEPARTED); // Cambió
        assertThat(trip.getRoute().getId()).isEqualTo(5L); // No cambió
        assertThat(trip.getBus().getId()).isEqualTo(6L); // No cambió
        assertThat(trip.getDate()).isEqualTo(LocalDate.now()); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyRouteId() {
        // Given
        var departureTime = OffsetDateTime.now();
        var arrivalTime = OffsetDateTime.now().plusHours(4);
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).build();
        
        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 11, 11))
                .departureAt(departureTime)
                .arrivalETA(arrivalTime)
                .status(TripStatus.SCHEDULED)
                .build();

        var updateRequest = new TripUpdateRequest(99L, null, null, null, null, null);

        // When
        mapper.patch(trip, updateRequest);

        // Then
        assertThat(trip.getRoute().getId()).isEqualTo(99L); // Cambió
        assertThat(trip.getBus().getId()).isEqualTo(2L); // No cambió
        assertThat(trip.getDate()).isEqualTo(LocalDate.of(2025, 11, 11)); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyBusId() {
        // Given
        var departureTime = OffsetDateTime.now();
        var arrivalTime = OffsetDateTime.now().plusHours(4);
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).build();
        
        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 11, 11))
                .departureAt(departureTime)
                .arrivalETA(arrivalTime)
                .status(TripStatus.SCHEDULED)
                .build();

        var updateRequest = new TripUpdateRequest(null, 88L, null, null, null, null);

        // When
        mapper.patch(trip, updateRequest);

        // Then
        assertThat(trip.getBus().getId()).isEqualTo(88L); // Cambió
        assertThat(trip.getRoute().getId()).isEqualTo(1L); // No cambió
        assertThat(trip.getStatus()).isEqualTo(TripStatus.SCHEDULED); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyDate() {
        // Given
        var departureTime = OffsetDateTime.now();
        var arrivalTime = OffsetDateTime.now().plusHours(4);
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).build();
        
        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 11, 11))
                .departureAt(departureTime)
                .arrivalETA(arrivalTime)
                .status(TripStatus.SCHEDULED)
                .build();

        var updateRequest = new TripUpdateRequest(null, null, LocalDate.of(2025, 12, 25), null, null, null);

        // When
        mapper.patch(trip, updateRequest);

        // Then
        assertThat(trip.getDate()).isEqualTo(LocalDate.of(2025, 12, 25)); // Cambió
        assertThat(trip.getRoute().getId()).isEqualTo(1L); // No cambió
        assertThat(trip.getBus().getId()).isEqualTo(2L); // No cambió
    }

    @Test
    void patch_shouldUpdateDepartureTimes() {
        // Given
        var oldDeparture = OffsetDateTime.now();
        var oldArrival = OffsetDateTime.now().plusHours(4);
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).build();
        
        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 11, 11))
                .departureAt(oldDeparture)
                .arrivalETA(oldArrival)
                .status(TripStatus.SCHEDULED)
                .build();

        var newDeparture = OffsetDateTime.now().plusHours(2);
        var newArrival = OffsetDateTime.now().plusHours(6);
        var updateRequest = new TripUpdateRequest(null, null, null, newDeparture, newArrival, null);

        // When
        mapper.patch(trip, updateRequest);

        // Then
        assertThat(trip.getDepartureAt()).isEqualTo(newDeparture); // Cambió
        assertThat(trip.getArrivalETA()).isEqualTo(newArrival); // Cambió
        assertThat(trip.getRoute().getId()).isEqualTo(1L); // No cambió
        assertThat(trip.getBus().getId()).isEqualTo(2L); // No cambió
        assertThat(trip.getStatus()).isEqualTo(TripStatus.SCHEDULED); // No cambió
    }

    @Test
    void patch_shouldNotModifyId() {
        // Given
        var departureTime = OffsetDateTime.now();
        var arrivalTime = OffsetDateTime.now().plusHours(4);
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).build();
        
        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 11, 11))
                .departureAt(departureTime)
                .arrivalETA(arrivalTime)
                .status(TripStatus.SCHEDULED)
                .build();

        var newDeparture = OffsetDateTime.now().plusDays(1);
        var newArrival = OffsetDateTime.now().plusDays(1).plusHours(5);
        var updateRequest = new TripUpdateRequest(
                3L,
                4L,
                LocalDate.of(2025, 12, 1),
                newDeparture,
                newArrival,
                TripStatus.CANCELLED
        );

        // When
        mapper.patch(trip, updateRequest);

        // Then
        assertThat(trip.getId()).isEqualTo(10L); // No cambió
    }
}