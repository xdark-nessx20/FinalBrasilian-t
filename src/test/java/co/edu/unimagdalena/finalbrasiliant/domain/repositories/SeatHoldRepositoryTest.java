package co.edu.unimagdalena.finalbrasiliant.domain.repositories;


import co.edu.unimagdalena.finalbrasiliant.domain.entities.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatHoldRepositoryTest extends AbstractRepository{

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    private User passenger1;
    private User passenger2;
    private Trip trip1;
    private Trip trip2;
    private SeatHold seatHold1;
    private SeatHold seatHold2;
    private SeatHold seatHold3;
    private SeatHold seatHold4;
    private SeatHold seatHold5;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        seatHoldRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();

        now = OffsetDateTime.now();

        Route route1 = routeRepository.save(Route.builder()
                .code("ZZZZ").routeName("a")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(BigDecimal.valueOf(415.0))
                .durationMin(360)
                .build());

        Route route2 = routeRepository.save(Route.builder()
                .code("ZZZA").routeName("b")
                .origin("Cali")
                .destination("Cartagena")
                .distanceKM(BigDecimal.valueOf(1100.0))
                .durationMin(900)
                .build());

        Bus bus1 = busRepository.save(Bus.builder()
                .plate("ABC123")
                .capacity(45)
                .build());

        Bus bus2 = busRepository.save(Bus.builder()
                .plate("XYZ789")
                .capacity(50)
                .build());

        passenger1 = createUser("Ana López", "ana@example.com", "3001234567");
        passenger2 = createUser("Carlos Ruiz", "carlos@example.com", "3007654321");

        trip1 = createTrip(route1, bus1, now.plusDays(1));
        trip2 = createTrip(route2, bus2, now.plusDays(2));

        seatHold1 = createSeatHold(trip1, "A12", passenger1,
                now.plusMinutes(8), SeatHoldStatus.HOLD);

        seatHold2 = createSeatHold(trip1, "A13", passenger1,
                now.plusMinutes(5), SeatHoldStatus.HOLD);

        seatHold3 = createSeatHold(trip1, "B15", passenger2,
                now.minusMinutes(5), SeatHoldStatus.EXPIRED);

        seatHold4 = createSeatHold(trip2, "C20", passenger1,
                now.plusMinutes(10), SeatHoldStatus.HOLD);

        seatHold5 = createSeatHold(trip2, "D22", passenger2,
                now.minusMinutes(15), SeatHoldStatus.EXPIRED);
    }

    private User createUser(String userName, String email, String phone) {
        return userRepository.save(User.builder()
                .userName(userName)
                .email(email)
                .phone(phone)
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashed_password")
                .build());
    }

    private Trip createTrip(Route route, Bus bus, OffsetDateTime departureAt) {
        return tripRepository.save(Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureAt(departureAt)
                .arrivalETA(departureAt.plusHours(6))
                .status(TripStatus.SCHEDULED)
                .build());
    }

    private SeatHold createSeatHold(Trip trip, String seatNumber, User passenger,
                                    OffsetDateTime expiresAt, SeatHoldStatus status) {
        return seatHoldRepository.save(SeatHold.builder()
                .trip(trip)
                .seatNumber(seatNumber)
                .passenger(passenger)
                .expiresAt(expiresAt)
                .status(status)
                .build());
    }

    @Test
    void shouldFindSeatHoldsByUserId() {
        List<SeatHold> result = seatHoldRepository.findByPassenger_Id(passenger1.getId());

        assertThat(result)
                .hasSize(3)
                .extracting(SeatHold::getId)
                .containsExactlyInAnyOrder(seatHold1.getId(), seatHold2.getId(), seatHold4.getId());
    }

    @Test
    void shouldFindSeatHoldsByTripId() {
        List<SeatHold> result = seatHoldRepository.findByTrip_Id(trip1.getId());

        assertThat(result)
                .hasSize(3)
                .extracting(SeatHold::getId)
                .containsExactlyInAnyOrder(seatHold1.getId(), seatHold2.getId(), seatHold3.getId());
    }

    @Test
    void shouldFindSeatHoldsByStatus() {
        List<SeatHold> result = seatHoldRepository.findByStatus(SeatHoldStatus.HOLD);

        assertThat(result)
                .hasSize(3)
                .extracting(SeatHold::getId)
                .containsExactlyInAnyOrder(seatHold1.getId(), seatHold2.getId(), seatHold4.getId());
    }

    @Test
    void shouldFindSeatHoldsByTripUserAndStatus() {
        List<SeatHold> result = seatHoldRepository.findByTrip_IdAndPassenger_IdAndStatus(
                trip1.getId(), passenger1.getId(), SeatHoldStatus.HOLD);

        assertThat(result)
                .hasSize(2)
                .extracting(SeatHold::getId)
                .containsExactlyInAnyOrder(seatHold1.getId(), seatHold2.getId());
    }

    @Test
    void shouldCheckIfSeatHoldExists() {
        boolean exists = seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(
                trip1.getId(), "A12", SeatHoldStatus.HOLD);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSeatHoldDoesNotExist() {
        boolean exists = seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(
                trip1.getId(), "Z99", SeatHoldStatus.HOLD);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldCountSeatHoldsByTripAndStatus() {
        long count = seatHoldRepository.countByTripIdAndStatus(
                trip1.getId(), SeatHoldStatus.HOLD);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindExpiredHolds() {
        List<SeatHold> result = seatHoldRepository.findExpiredHolds(now.plusMinutes(5));

        assertThat(result)
                .hasSize(1)
                .extracting(SeatHold::getId).containsExactlyInAnyOrder(seatHold2.getId());
    }

    @Test
    void shouldNotFindExpiredHoldsWhenAllAreActive() {
        OffsetDateTime futureTime = now.plusHours(1);
        List<SeatHold> result = seatHoldRepository.findExpiredHolds(futureTime);

        assertThat(result)
                .hasSize(3)
                .extracting(SeatHold::getId)
                .containsExactlyInAnyOrder(seatHold1.getId(), seatHold2.getId(), seatHold4.getId());
    }

    @Test
    void shouldFindExpiredHoldsOnlyWithHoldStatus() {
        OffsetDateTime checkTime = now.plusMinutes(6);
        List<SeatHold> result = seatHoldRepository.findExpiredHolds(checkTime);

        assertThat(result)
                .hasSize(1)
                .extracting(SeatHold::getId)
                .containsExactly(seatHold2.getId());
    }
}