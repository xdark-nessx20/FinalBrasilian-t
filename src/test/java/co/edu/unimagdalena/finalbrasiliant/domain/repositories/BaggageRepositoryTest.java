package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
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

class BaggageRepositoryTest extends AbstractRepository {

    @Autowired
    private BaggageRepository baggageRepository;

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
    private Ticket ticket1;
    private Baggage baggage1;
    private Baggage baggage2;
    private Baggage baggage3;
    private Baggage baggage4;

    @BeforeEach
    void setUp() {
        baggageRepository.deleteAll();
        ticketRepository.deleteAll();
        tripRepository.deleteAll();
        stopRepository.deleteAll();
        userRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();

        Route route = routeRepository.save(Route.builder().code("ZZZZZZ").routeName("a")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(BigDecimal.valueOf(415.0))
                .durationMin(360)
                .build());

        Bus bus = busRepository.save(Bus.builder()
                .plate("ABC123")
                .capacity(45)
                .build());

        Trip trip = createTrip(route, bus);

        Stop stop1 = stopRepository.save(Stop.builder()
                .name("Terminal Bogotá")
                .route(route)
                .stopOrder(1)
                .build());

        Stop stop2 = stopRepository.save(Stop.builder()
                .name("Terminal Medellín")
                .route(route)
                .stopOrder(2)
                .build());

        passenger1 = createUser("Ana López", "ana@example.com", "3001234567");
        User passenger2 = createUser("Carlos Ruiz", "carlos@example.com", "3007654321");


        ticket1 = createTicket(trip, passenger1, stop1, stop2, "A12", new BigDecimal("50000.00"));
        Ticket ticket2 = createTicket(trip, passenger1, stop1, stop2, "A13", new BigDecimal("50000.00"));
        Ticket ticket3 = createTicket(trip, passenger2, stop1, stop2, "B15", new BigDecimal("50000.00"));

        baggage1 = createBaggage(ticket1, new BigDecimal("15.50"), new BigDecimal("25000.00"), "TAG001");
        baggage2 = createBaggage(ticket1, new BigDecimal("8.30"), new BigDecimal("15000.00"), "TAG002");
        baggage3 = createBaggage(ticket2, new BigDecimal("20.00"), new BigDecimal("35000.00"), "TAG003");
        baggage4 = createBaggage(ticket3, new BigDecimal("5.75"), new BigDecimal("10000.00"), "TAG004");
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

    private Trip createTrip(Route route, Bus bus) {
        OffsetDateTime now = OffsetDateTime.now();
        return tripRepository.save(Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureAt(now.plusDays(1))
                .arrivalETA(now.plusDays(1).plusHours(6))
                .build());
    }

    private Ticket createTicket(Trip trip, User passenger, Stop from, Stop to,
                                String seatNumber, BigDecimal price) {
        return ticketRepository.save(Ticket.builder()
                .trip(trip)
                .passenger(passenger)
                .fromStop(from)
                .toStop(to)
                .seatNumber(seatNumber)
                .price(price)
                .paymentMethod(PaymentMethod.CARD)
                .qrCode("QR_" + seatNumber)
                .build());
    }

    private Baggage createBaggage(Ticket ticket, BigDecimal weightKg,
                                  BigDecimal fee, String tagCode) {
        return baggageRepository.save(Baggage.builder()
                .ticket(ticket)
                .weightKg(weightKg)
                .fee(fee)
                .tagCode(tagCode)
                .build());
    }

    @Test
    void shouldFindBaggageByTagCode() {
        Optional<Baggage> result = baggageRepository.findByTagCode("TAG001");

        assertThat(result).isPresent()
                .hasValueSatisfying(baggage -> {
                    assertThat(baggage.getId()).isEqualTo(baggage1.getId());
                    assertThat(baggage.getTagCode()).isEqualTo("TAG001");
                    assertThat(baggage.getWeightKg()).isEqualByComparingTo(new BigDecimal("15.50"));
                });
    }

    @Test
    void shouldFindBaggagesByWeightGreaterThanOrEqual() {
        BigDecimal minWeight = new BigDecimal("15.00");

        Page<Baggage> result = baggageRepository.findByWeightKgGreaterThanEqual(
                minWeight, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Baggage::getId)
                .containsExactlyInAnyOrder(baggage1.getId(), baggage3.getId());
    }

    @Test
    void shouldFindBaggagesByWeightLessThanOrEqual() {
        BigDecimal maxWeight = new BigDecimal("10.00");

        Page<Baggage> result = baggageRepository.findByWeightKgLessThanEqual(
                maxWeight, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Baggage::getId)
                .containsExactlyInAnyOrder(baggage2.getId(), baggage4.getId());
    }

    @Test
    void shouldFindBaggagesByPassengerId() {
        List<Baggage> result = baggageRepository.findByTicket_Passenger_Id(passenger1.getId());

        assertThat(result)
                .hasSize(3)
                .extracting(Baggage::getId)
                .containsExactlyInAnyOrder(baggage1.getId(), baggage2.getId(), baggage3.getId());
    }

    @Test
    void shouldFindBaggageByIdWithAllDetails() {
        Optional<Baggage> result = baggageRepository.findByIdWithAllDetails(baggage1.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(baggage -> {
                    assertThat(baggage.getId()).isEqualTo(baggage1.getId());
                    assertThat(baggage.getTicket())
                            .isNotNull()
                            .satisfies(ticket -> {
                                assertThat(ticket.getId()).isEqualTo(ticket1.getId());
                                assertThat(ticket.getSeatNumber()).isEqualTo("A12");
                                assertThat(ticket.getStatus()).isEqualTo(TicketStatus.SOLD);
                            });
                });
    }
}
