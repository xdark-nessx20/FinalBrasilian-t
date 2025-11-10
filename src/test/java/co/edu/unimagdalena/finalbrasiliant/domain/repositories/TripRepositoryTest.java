package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TripRepositoryTest extends AbstractRepository {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    private Route route1;
    private Route route2;
    private Bus bus1;
    private Bus bus2;
    private Trip trip1;
    private Trip trip2;
    private Trip trip3;
    private Trip trip4;
    private Trip trip5;
    private OffsetDateTime baseTime;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();
        
        baseTime = OffsetDateTime.now();

        route1 = routeRepository.save(Route.builder()
                .code("RT001")
                .routeName("Ruta Bogotá - Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("415.50"))
                .durationMin(360)
                .build());

        route2 = routeRepository.save(Route.builder()
                .code("RT002")
                .routeName("Ruta Bogotá - Cali")
                .origin("Bogotá")
                .destination("Cali")
                .distanceKM(new BigDecimal("450.75"))
                .durationMin(480)
                .build());

        bus1 = busRepository.save(Bus.builder()
                .plate("ABC123")
                .capacity(45)
                .status(BusStatus.AVAILABLE)
                .build());

        bus2 = busRepository.save(Bus.builder()
                .plate("XYZ789")
                .capacity(30)
                .status(BusStatus.AVAILABLE)
                .build());

        trip1 = createTrip(
                route1,
                bus1,
                LocalDate.now(),
                baseTime.plusHours(2),
                baseTime.plusHours(8),
                TripStatus.SCHEDULED
        );

        trip2 = createTrip(
                route1,
                bus2,
                LocalDate.now().plusDays(1),
                baseTime.plusDays(1).plusHours(3),
                baseTime.plusDays(1).plusHours(9),
                TripStatus.SCHEDULED
        );

        trip3 = createTrip(
                route2,
                bus1,
                LocalDate.now().plusDays(2),
                baseTime.plusDays(2).plusHours(1),
                baseTime.plusDays(2).plusHours(9),
                TripStatus.BOARDING
        );

        trip4 = createTrip(
                route1,
                bus2,
                LocalDate.now(),
                baseTime.plusHours(10),
                baseTime.plusHours(16),
                TripStatus.DEPARTED
        );

        trip5 = createTrip(
                route2,
                bus1,
                LocalDate.now().plusDays(3),
                baseTime.plusDays(3).plusHours(4),
                baseTime.plusDays(3).plusHours(12),
                TripStatus.CANCELLED
        );
    }

    private Trip createTrip(Route route, Bus bus, LocalDate date,
                           OffsetDateTime departureAt, OffsetDateTime arrivalETA,
                           TripStatus status) {
        return tripRepository.save(Trip.builder()
                .route(route)
                .bus(bus)
                .date(date)
                .departureAt(departureAt)
                .arrivalETA(arrivalETA)
                .status(status)
                .build());
    }

    @Test
    void shouldFindTripByRouteId() {
        var result = tripRepository.findByRoute_Id(route1.getId());

        assertThat(result).hasSize(3);
    }

    @Test
    void shouldFindAllTripsByRouteId() {
        List<Trip> result = tripRepository.findAllByRoute_Id(route1.getId());

        assertThat(result)
                .hasSize(3)
                .extracting(Trip::getId)
                .containsExactlyInAnyOrder(trip1.getId(), trip2.getId(), trip4.getId());

        assertThat(result)
                .allSatisfy(trip -> assertThat(trip.getRoute().getId()).isEqualTo(route1.getId()));
    }

    @Test
    void shouldFindAllTripsBySecondRoute() {
        List<Trip> result = tripRepository.findAllByRoute_Id(route2.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(Trip::getId)
                .containsExactlyInAnyOrder(trip3.getId(), trip5.getId());
    }

    @Test
    void shouldFindTripByBusId() {
        var result = tripRepository.findByBus_Id(bus2.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFindAllTripsByDepartureAtBetween() {
        OffsetDateTime start = baseTime.plusHours(1);
        OffsetDateTime end = baseTime.plusHours(5);

        List<Trip> result = tripRepository.findAllByDepartureAtBetween(start, end);

        assertThat(result)
                .hasSize(1)
                .extracting(Trip::getId)
                .containsExactly(trip1.getId());

        assertThat(result.get(0).getDepartureAt())
                .isAfterOrEqualTo(start)
                .isBeforeOrEqualTo(end);
    }

    @Test
    void shouldFindMultipleTripsInDepartureRange() {
        OffsetDateTime start = baseTime.plusHours(1);
        OffsetDateTime end = baseTime.plusHours(11);

        List<Trip> result = tripRepository.findAllByDepartureAtBetween(start, end);

        assertThat(result)
                .hasSize(2)
                .extracting(Trip::getId)
                .containsExactlyInAnyOrder(trip1.getId(), trip4.getId());
    }

    @Test
    void shouldFindAllTripsByArrivalETABetween() {
        OffsetDateTime start = baseTime.plusHours(7);
        OffsetDateTime end = baseTime.plusHours(10);

        List<Trip> result = tripRepository.findAllByArrivalETABetween(start, end);

        assertThat(result)
                .hasSize(1)
                .extracting(Trip::getId)
                .containsExactly(trip1.getId());

        assertThat(result.get(0).getArrivalETA())
                .isAfterOrEqualTo(start)
                .isBeforeOrEqualTo(end);
    }

    @Test
    void shouldFindAllTripsByStatusScheduled() {
        List<Trip> result = tripRepository.findAllByStatus(TripStatus.SCHEDULED);

        assertThat(result)
                .hasSize(2)
                .extracting(Trip::getId)
                .containsExactlyInAnyOrder(trip1.getId(), trip2.getId());

        assertThat(result)
                .allSatisfy(trip -> assertThat(trip.getStatus()).isEqualTo(TripStatus.SCHEDULED));
    }

    @Test
    void shouldFindAllTripsByStatusBoarding() {
        List<Trip> result = tripRepository.findAllByStatus(TripStatus.BOARDING);

        assertThat(result)
                .hasSize(1)
                .extracting(Trip::getId)
                .containsExactly(trip3.getId());

        assertThat(result.get(0).getStatus()).isEqualTo(TripStatus.BOARDING);
    }

    @Test
    void shouldFindAllTripsByStatusDeparted() {
        List<Trip> result = tripRepository.findAllByStatus(TripStatus.DEPARTED);

        assertThat(result)
                .hasSize(1)
                .extracting(Trip::getId)
                .containsExactly(trip4.getId());
    }

    @Test
    void shouldFindAllTripsByStatusCancelled() {
        List<Trip> result = tripRepository.findAllByStatus(TripStatus.CANCELLED);

        assertThat(result)
                .hasSize(1)
                .extracting(Trip::getId)
                .containsExactly(trip5.getId());
    }

    @Test
    void shouldFindAllTripsByRouteIdAndStatus() {
        List<Trip> result = tripRepository.findAllByRoute_IdAndStatus(
                route1.getId(), TripStatus.SCHEDULED);

        assertThat(result)
                .hasSize(2)
                .extracting(Trip::getId)
                .containsExactlyInAnyOrder(trip1.getId(), trip2.getId());

        assertThat(result)
                .allSatisfy(trip -> {
                    assertThat(trip.getRoute().getId()).isEqualTo(route1.getId());
                    assertThat(trip.getStatus()).isEqualTo(TripStatus.SCHEDULED);
                });
    }

    @Test
    void shouldFindTripsByRouteAndDepartedStatus() {
        List<Trip> result = tripRepository.findAllByRoute_IdAndStatus(
                route1.getId(), TripStatus.DEPARTED);

        assertThat(result)
                .hasSize(1)
                .extracting(Trip::getId)
                .containsExactly(trip4.getId());
    }

    @Test
    void shouldFindAllTripsByDate() {
        List<Trip> result = tripRepository.findAllByDate(LocalDate.now());

        assertThat(result)
                .hasSize(2)
                .extracting(Trip::getId)
                .containsExactlyInAnyOrder(trip1.getId(), trip4.getId());

        assertThat(result)
                .allSatisfy(trip -> assertThat(trip.getDate()).isEqualTo(LocalDate.now()));
    }

    @Test
    void shouldFindTripsByFutureDate() {
        List<Trip> result = tripRepository.findAllByDate(LocalDate.now().plusDays(1));

        assertThat(result)
                .hasSize(1)
                .extracting(Trip::getId)
                .containsExactly(trip2.getId());
    }
}