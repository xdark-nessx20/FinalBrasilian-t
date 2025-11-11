package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TicketRepositoryTest extends AbstractRepository {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    private User passenger1;
    private Trip trip1;
    private Stop stop1;
    private Stop stop3;
    private Ticket ticket1;
    private Ticket ticket2;
    private Ticket ticket3;
    private Ticket ticket4;
    private Ticket ticket5;
    private Ticket ticket6;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        tripRepository.deleteAll();
        stopRepository.deleteAll();
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
                .code("ZZZW").routeName("b")
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

        stop1 = stopRepository.save(Stop.builder()
                .route(route1)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build());

        Stop stop2 = stopRepository.save(Stop.builder()
                .route(route1)
                .name("Peaje La Línea")
                .stopOrder(2)
                .lat(4.5000)
                .lng(-74.5000)
                .build());

        stop3 = stopRepository.save(Stop.builder()
                .route(route1)
                .name("Terminal Medellín")
                .stopOrder(3)
                .lat(6.2442)
                .lng(-75.5812)
                .build());

        trip1 = createTrip(route1, bus1, now.plusDays(1), TripStatus.SCHEDULED);
        Trip trip2 = createTrip(route2, bus2, now.plusDays(2), TripStatus.BOARDING);

        passenger1 = createUser("Ana López", "ana@example.com", "3001234567");
        User passenger2 = createUser("Carlos Ruiz", "carlos@example.com", "3007654321");
        User passenger3 = createUser("María García", "maria@example.com", "3009876543");

        ticket1 = createTicket(trip1, passenger1, stop1, stop3, "A1",
                new BigDecimal("50000.00"), PaymentMethod.CARD, TicketStatus.SOLD, "QR001");

        ticket2 = createTicket(trip1, passenger2, stop1, stop2, "A2",
                new BigDecimal("30000.00"), PaymentMethod.CASH, TicketStatus.SOLD, "QR002");

        ticket3 = createTicket(trip1, passenger1, stop2, stop3, "A3",
                new BigDecimal("25000.00"), PaymentMethod.CARD, TicketStatus.CANCELLED, "QR003");

        ticket4 = createTicket(trip2, passenger3, stop1, stop3, "B1",
                new BigDecimal("80000.00"), PaymentMethod.CARD, TicketStatus.SOLD, "QR004");

        ticket5 = createTicket(trip1, passenger2, stop1, stop3, "A4",
                new BigDecimal("50000.00"), PaymentMethod.CASH, TicketStatus.NO_SHOW, "QR005");

        ticket6 = createTicket(trip2, passenger1, stop1, stop3, "B2",
                new BigDecimal("80000.00"), PaymentMethod.CARD, TicketStatus.SOLD, "QR006");
    }

    private User createUser(String userName, String email, String phone) {
        return userRepository.save(User.builder()
                .userName(userName)
                .email(email)
                .phone(phone)
                .role(Role.PASSENGER)
                .status(true)
                .passwordHash("hashed_password")
                .build());
    }

    private Trip createTrip(Route route, Bus bus, OffsetDateTime departureAt, TripStatus status) {
        return tripRepository.save(Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureAt(departureAt)
                .arrivalETA(departureAt.plusHours(6))
                .status(status)
                .build());
    }

    private Ticket createTicket(Trip trip, User passenger, Stop from, Stop to,
                                String seatNumber, BigDecimal price, PaymentMethod paymentMethod,
                                TicketStatus status, String qrCode) {
        return ticketRepository.save(Ticket.builder()
                .trip(trip)
                .passenger(passenger)
                .fromStop(from)
                .toStop(to)
                .seatNumber(seatNumber)
                .price(price)
                .paymentMethod(paymentMethod)
                .status(status)
                .qrCode(qrCode)
                .build());
    }

    @Test
    void shouldFindTicketByTripIdAndSeatNumber() {
        Optional<Ticket> result = ticketRepository.findByTrip_IdAndSeatNumber(
                trip1.getId(), "A1");

        assertThat(result).isPresent()
                .hasValueSatisfying(ticket -> {
                    assertThat(ticket.getId()).isEqualTo(ticket1.getId());
                    assertThat(ticket.getSeatNumber()).isEqualTo("A1");
                    assertThat(ticket.getTrip().getId()).isEqualTo(trip1.getId());
                });
    }

    @Test
    void shouldFindTicketByQrCodeWithAllDetails() {
        Optional<Ticket> result = ticketRepository.findByQrCode("QR001");

        assertThat(result).isPresent()
                .hasValueSatisfying(ticket -> {
                    assertThat(ticket.getId()).isEqualTo(ticket1.getId());
                    assertThat(ticket.getQrCode()).isEqualTo("QR001");
                    assertThat(ticket.getFromStop()).isNotNull();
                    assertThat(ticket.getToStop()).isNotNull();
                    assertThat(ticket.getTrip()).isNotNull();
                    assertThat(ticket.getPassenger()).isNotNull();
                    assertThat(ticket.getFromStop().getName()).isEqualTo("Terminal Bogotá");
                    assertThat(ticket.getToStop().getName()).isEqualTo("Terminal Medellín");
                });
    }

    @Test
    void shouldFindTicketsByPaymentMethod() {
        Page<Ticket> result = ticketRepository.findByPaymentMethod(
                PaymentMethod.CARD, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(4)
                .extracting(Ticket::getId)
                .containsExactlyInAnyOrder(ticket1.getId(), ticket3.getId(), ticket4.getId(), ticket6.getId());
    }

    @Test
    void shouldFindTicketsByStatus() {
        Page<Ticket> result = ticketRepository.findByStatus(
                TicketStatus.SOLD, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(4)
                .extracting(Ticket::getId)
                .containsExactlyInAnyOrder(ticket1.getId(), ticket2.getId(), ticket4.getId(), ticket6.getId());
    }

    @Test
    void shouldFindTicketsByCreatedAtBetween() {
        OffsetDateTime start = now.minusHours(1);
        OffsetDateTime end = now.plusHours(1);

        Page<Ticket> result = ticketRepository.findByCreatedAtBetween(
                start, end, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(6);
    }

    @Test
    void shouldFindTicketsByStretchWithBothStops() {
        Page<Ticket> result = ticketRepository.findAllByStretch(
                stop1.getId(), stop3.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(4)
                .extracting(Ticket::getId)
                .containsExactlyInAnyOrder(ticket1.getId(), ticket4.getId(), ticket5.getId(), ticket6.getId());
    }

    @Test
    void shouldFindTicketsByStretchWithOnlyFromStop() {
        Page<Ticket> result = ticketRepository.findAllByStretch(
                stop1.getId(), null, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(5)
                .extracting(Ticket::getId)
                .containsExactlyInAnyOrder(
                        ticket1.getId(), ticket2.getId(),
                        ticket4.getId(), ticket5.getId(), ticket6.getId()
                );
    }

    @Test
    void shouldFindTicketsByStretchWithOnlyToStop() {
        Page<Ticket> result = ticketRepository.findAllByStretch(
                null, stop3.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(5)
                .extracting(Ticket::getId)
                .containsExactlyInAnyOrder(
                        ticket1.getId(), ticket3.getId(),
                        ticket4.getId(), ticket5.getId(), ticket6.getId()
                );
    }

    @Test
    void shouldFindTicketsByPassengerId() {
        List<Ticket> result = ticketRepository.findByPassenger_Id(passenger1.getId());

        assertThat(result)
                .hasSize(3)
                .extracting(Ticket::getId)
                .containsExactlyInAnyOrder(ticket1.getId(), ticket3.getId(), ticket6.getId());
    }

    @Test
    void shouldFindTicketsByTripId() {
        List<Ticket> result = ticketRepository.findByTrip_Id(trip1.getId());

        assertThat(result)
                .hasSize(4)
                .extracting(Ticket::getId)
                .containsExactlyInAnyOrder(ticket1.getId(), ticket2.getId(), ticket3.getId(), ticket5.getId());
    }

    @Test
    void shouldCountByStatusAndOptionalDateRangeWithAllParams() {
        OffsetDateTime start = now.minusHours(1);
        OffsetDateTime end = now.plusHours(1);

        long count = ticketRepository.countByStatusAndOptionalDateRange(
                TicketStatus.SOLD, start, end);

        assertThat(count).isEqualTo(4);
    }

    @Test
    void shouldCountByStatusAndOptionalDateRangeWithStartDate() {
        OffsetDateTime start = now.minusHours(1);

        long count = ticketRepository.countByStatusAndOptionalDateRange(
                TicketStatus.CANCELLED, start, null);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldSumPriceByStatus() {
        BigDecimal total = ticketRepository.sumPriceByStatus(TicketStatus.SOLD);

        assertThat(total)
                .isNotNull()
                .isEqualByComparingTo(new BigDecimal("240000.00"));
    }

    @Test
    void shouldSumPriceByPassengerId() {
        BigDecimal total = ticketRepository.sumPriceByPassenger_Id(passenger1.getId());

        assertThat(total)
                .isNotNull()
                .isEqualByComparingTo(new BigDecimal("130000.00"));
    }
}