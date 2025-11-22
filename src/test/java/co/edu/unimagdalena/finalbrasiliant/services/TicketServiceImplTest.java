package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TicketDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.*;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.*;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.impl.TicketServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.TicketMapper;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepo;

    @Mock
    private StopRepository stopRepo;

    @Mock
    private TripRepository tripRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private SeatRepository seatRepo;

    @Mock
    private SeatHoldRepository seatHoldRepo;

    @Mock
    private FareRuleRepository fareRuleRepo;

    @Mock
    private ConfigService configService;

    @Spy
    private TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @Spy
    private NotificationService notif;

    @InjectMocks
    private TicketServiceImpl service;


    @Test
    void shouldCreateTicketSuccessfully() {
        // Given
        var trip = Trip.builder().id(1L).route(
                Route.builder().id(101L).build()
        ).build();
        var fromStop = Stop.builder().id(3L).stopOrder(1).build();
        var toStop = Stop.builder().id(4L).stopOrder(3).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();
        var fareRule = FareRule.builder().id(10L).basePrice(BigDecimal.valueOf(100000.00)).route(
                Route.builder().id(101L).routeName("a").code("ZZZZ").origin("x").destination("y")
                        .distanceKM(BigDecimal.valueOf(200)).durationMin(240).build()
        ).dynamicPricing(DynamicPricing.OFF).fromStop(fromStop).toStop(toStop).discounts(Map.of(
                "STUDENT", BigDecimal.valueOf(.50)
        )).build();

        var request = new TicketCreateRequest(
                2L, "A12", 3L, 4L,
                PaymentMethod.CARD, "STUDENT"
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(4L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepo.findByRouteIdAndFromStopIdAndToStopId(101L, 3L, 4L))
                .thenReturn(Optional.of(fareRule));

        when(ticketRepo.existsOverlap(1L, "A12", 1, 3)).thenReturn(false);
        when(userRepo.findById(2L)).thenReturn(Optional.of(passenger));
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.HOLD))
                .thenReturn(false);

        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(10L);
            t.setCreatedAt(OffsetDateTime.now());
            t.setStatus(TicketStatus.CREATED);
            return t;
        });

        // When
        var response = service.create(1L, request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A12");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(response.passenger().id()).isEqualTo(2L);

        verify(ticketRepo, times(1)).save(any(Ticket.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripNotExists() {
        // Given
        var request = new TicketCreateRequest(
                2L, "A12", 3L, 4L,
                PaymentMethod.CARD, "STUDENT"
        );

        when(tripRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(99L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");

        verify(ticketRepo, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFromStopNotExists() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var request = new TicketCreateRequest(
                2L, "A12", 99L, 4L,
                PaymentMethod.CARD, "STUDENT"
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 99 not found");

        verify(ticketRepo, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenToStopNotExists() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var fromStop = Stop.builder().id(3L).stopOrder(1).build();
        var request = new TicketCreateRequest(
                2L, "A12", 3L, 99L,
                PaymentMethod.CARD, "STUDENT"
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 99 not found");

        verify(ticketRepo, never()).save(any());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenSeatOverlaps() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var fromStop = Stop.builder().id(3L).stopOrder(1).build();
        var toStop = Stop.builder().id(4L).stopOrder(3).build();

        var request = new TicketCreateRequest(
                2L, "A12", 3L, 4L,
                PaymentMethod.CARD, "STUDENT"
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(4L)).thenReturn(Optional.of(toStop));
        when(ticketRepo.existsOverlap(1L, "A12", 1, 3)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Already exists a Ticket for the A12 seat");

        verify(ticketRepo, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPassengerNotExists() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var fromStop = Stop.builder().id(3L).stopOrder(1).build();
        var toStop = Stop.builder().id(4L).stopOrder(3).build();

        var request = new TicketCreateRequest(
                99L, "A12", 3L, 4L,
                PaymentMethod.CARD, "STUDENT"
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(4L)).thenReturn(Optional.of(toStop));
        when(ticketRepo.existsOverlap(1L, "A12", 1, 3)).thenReturn(false);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Passenger 99 not found");

        verify(ticketRepo, never()).save(any());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenSeatIsHeldByAnotherPassenger() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var fromStop = Stop.builder().id(3L).stopOrder(1).build();
        var toStop = Stop.builder().id(4L).stopOrder(3).build();
        var passenger = User.builder().id(2L).build();

        var request = new TicketCreateRequest(
                2L, "A12", 3L, 4L,
                PaymentMethod.CARD, "STUDENT"
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(4L)).thenReturn(Optional.of(toStop));
        when(ticketRepo.existsOverlap(1L, "A12", 1, 3)).thenReturn(false);
        when(userRepo.findById(2L)).thenReturn(Optional.of(passenger));
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.HOLD))
                .thenReturn(true);
        when(seatHoldRepo.findByTrip_IdAndPassenger_IdAndStatus(1L, 2L, SeatHoldStatus.HOLD))
                .thenReturn(List.of()); // No tiene holds este pasajero

        // When / Then
        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("The seat A12 is hold by another passenger");

        verify(ticketRepo, never()).save(any());
    }

    @Test
    void shouldGetTicketById() {
        // Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(1L).bus(bus).departureAt(OffsetDateTime.now()).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();
        var fromStop = Stop.builder().id(3L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(4L).name("Medellín").stopOrder(3).build();

        var ticket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .createdAt(OffsetDateTime.now())
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .build();

        when(ticketRepo.findById(10L)).thenReturn(Optional.of(ticket));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A12");
        assertThat(response.passenger().userName()).isEqualTo("Juan");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentTicket() {
        // Given
        when(ticketRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket 99 not found");
    }

    @Test
    void shouldUpdateTicket() {
        // Given
        var ticket = Ticket.builder()
                .id(10L)
                .seatNumber("A12")
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .trip(Trip.builder().id(777L).status(TripStatus.SCHEDULED)
                        .departureAt(OffsetDateTime.now().plusHours(23)).build())
                .passenger(User.builder().id(100001L).userName("Felipe Neduro").phone("3135467890").build())
                .build();

        var updateRequest = new TicketUpdateRequest(
                "B15",
                new BigDecimal("60000.00"),
                PaymentMethod.CARD,
                TicketStatus.CANCELLED
        );

        when(ticketRepo.findByIdWithAll(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        //when(configService.getValue("second.refund.percent")).thenReturn(BigDecimal.valueOf(.60));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.seatNumber()).isEqualTo("B15");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("60000.00"));
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(response.status()).isEqualTo(TicketStatus.CANCELLED);

        verify(ticketRepo).save(any(Ticket.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentTicket() {
        // Given
        var updateRequest = new TicketUpdateRequest("B15", null, null, null);
        //when(ticketRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket 99 not found");

        verify(ticketRepo, never()).save(any());
    }

    @Test
    void shouldDeleteTicket() {
        // When
        service.delete(10L);

        // Then
        verify(ticketRepo).deleteById(10L);
    }

    @Test
    void shouldGetTicketByTripAndSeat() {
        // Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(1L).bus(bus).departureAt(OffsetDateTime.now()).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();
        var fromStop = Stop.builder().id(3L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(4L).name("Medellín").stopOrder(3).build();
        var seat = Seat.builder().id(1L).number("A12").bus(bus).build();

        var ticket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .createdAt(OffsetDateTime.now())
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .build();

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(seatRepo.findByNumberAndBus_Id("A12", 1L)).thenReturn(Optional.of(seat));
        when(ticketRepo.findByTrip_IdAndSeatNumber(1L, "A12")).thenReturn(Optional.of(ticket));

        // When
        var response = service.getByTripSeat(1L, "A12");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A12");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripNotFoundForTripSeat() {
        // Given
        when(tripRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByTripSeat(99L, "A12"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripHasNoBus() {
        // Given
        var trip = Trip.builder().id(1L).bus(null).build();
        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));

        // When / Then
        assertThatThrownBy(() -> service.getByTripSeat(1L, "A12"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 1 hasn't bus assigned yet");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenSeatNotFoundInBus() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var trip = Trip.builder().id(1L).bus(bus).build();

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(seatRepo.findByNumberAndBus_Id("Z99", 1L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByTripSeat(1L, "Z99"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Seat number Z99 not found");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTicketNotFoundForTripSeat() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var trip = Trip.builder().id(1L).bus(bus).build();
        var seat = Seat.builder().id(1L).number("A12").build();

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(seatRepo.findByNumberAndBus_Id("A12", 1L)).thenReturn(Optional.of(seat));
        when(ticketRepo.findByTrip_IdAndSeatNumber(1L, "A12")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByTripSeat(1L, "A12"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket 1 not found");
    }

    @Test
    void shouldListTicketsByStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(1L).bus(bus).departureAt(OffsetDateTime.now()).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();
        var fromStop = Stop.builder().id(3L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(4L).name("Medellín").stopOrder(3).build();

        var ticket1 = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .fromStop(fromStop)
                .toStop(toStop)
                .status(TicketStatus.SOLD)
                .build();

        var ticket2 = Ticket.builder()
                .id(2L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A13")
                .fromStop(fromStop)
                .toStop(toStop)
                .status(TicketStatus.SOLD)
                .build();

        var page = new PageImpl<>(List.of(ticket1, ticket2));
        when(ticketRepo.findByStatus(TicketStatus.SOLD, pageable)).thenReturn(page);

        // When
        var result = service.listByStatus(TicketStatus.SOLD, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(t -> t.status() == TicketStatus.SOLD);
    }

    @Test
    void shouldListTicketsByPaymentMethod() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(1L).bus(bus).departureAt(OffsetDateTime.now()).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();
        var fromStop = Stop.builder().id(3L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(4L).name("Medellín").stopOrder(3).build();

        var ticket1 = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .fromStop(fromStop)
                .toStop(toStop)
                .paymentMethod(PaymentMethod.CARD)
                .build();

        var page = new PageImpl<>(List.of(ticket1));
        when(ticketRepo.findByPaymentMethod(PaymentMethod.CARD, pageable)).thenReturn(page);

        // When
        var result = service.listByPaymentMethod(PaymentMethod.CARD, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().paymentMethod()).isEqualTo(PaymentMethod.CARD);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEndBeforeStart() {
        // Given
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.minusDays(1);
        Pageable pageable = PageRequest.of(0, 10);

        // When / Then
        assertThatThrownBy(() -> service.listByCreatedAt(start, end, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time can't be before start time");
    }

    @Test
    void shouldListTicketsByPassenger() {
        // Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(1L).bus(bus).departureAt(OffsetDateTime.now()).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();
        var fromStop = Stop.builder().id(3L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(4L).name("Medellín").stopOrder(3).build();

        var ticket1 = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        var ticket2 = Ticket.builder()
                .id(2L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A13")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        when(userRepo.findById(2L)).thenReturn(Optional.of(passenger));
        when(ticketRepo.findByPassenger_Id(2L)).thenReturn(List.of(ticket1, ticket2));

        // When
        var result = service.listByPassenger(2L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.passenger().id().equals(2L));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPassengerNotExistsForList() {
        // Given
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.listByPassenger(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Passenger 99 not found");
    }

    @Test
    void shouldListTicketsByTrip() {
        // Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(1L).bus(bus).departureAt(OffsetDateTime.now()).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();
        var fromStop = Stop.builder().id(3L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(4L).name("Medellín").stopOrder(3).build();

        var ticket1 = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        var ticket2 = Ticket.builder()
                .id(2L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A13")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(ticketRepo.findByTrip_Id(1L)).thenReturn(List.of(ticket1, ticket2));

        // When
        var result = service.listByTrip(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.trip().id().equals(1L));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripNotExistsForList() {
        // Given
        when(tripRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.listByTrip(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");
    }

    @Test
    void shouldListTicketsByStretch() {
        // Given
        var fromStop = Stop.builder().id(1L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(3L).name("Medellín").stopOrder(3).build();

        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(1L).bus(bus).departureAt(OffsetDateTime.now()).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();

        var ticket1 = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        when(stopRepo.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(toStop));
        when(ticketRepo.findAllByStretch(1L, 3L)).thenReturn(List.of(ticket1));

        // When
        var result = service.listByStretch(1L, 3L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().fromStop().id()).isEqualTo(1L);
        assertThat(result.getFirst().toStop().id()).isEqualTo(3L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFromStopNotExistsForStretch() {
        // Given
        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.listByStretch(99L, 3L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 99 not found");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenToStopNotExistsForStretch() {
        // Given
        var fromStop = Stop.builder().id(1L).build();
        when(stopRepo.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.listByStretch(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 99 not found");
    }

    /*@Test
    void shouldDoNothingWhenNoTicketsToSetNoShow() {
        // Given
        when(ticketRepo.findByPassengerNoShow()).thenReturn(List.of());

        // When
        service.setTicketsNoShow();

        // Then
        verify(ticketRepo).findByPassengerNoShow();
        verify(mapper, never()).patch(any(), any());
    }*/

    @Test
    void shouldCreateTicketWhenPassengerHoldsSameSeat() {
        // Given
        var route = Route.builder().id(101L).routeName("a").code("ZZZZ").origin("x").destination("y")
                .distanceKM(BigDecimal.valueOf(200)).durationMin(240).build();
        var trip = Trip.builder().id(1L).route(route).build();
        var fromStop = Stop.builder().id(3L).stopOrder(1).build();
        var toStop = Stop.builder().id(4L).stopOrder(3).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();
        
        var seatHold = SeatHold.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .status(SeatHoldStatus.HOLD)
                .build();
        var fareRule = FareRule.builder().id(10L).basePrice(BigDecimal.valueOf(100000.00)).route(route)
                .dynamicPricing(DynamicPricing.OFF).fromStop(fromStop).toStop(toStop).discounts(
                        Map.of("STUDENT", BigDecimal.valueOf(.50))).build();

        var request = new TicketCreateRequest(
                2L, "A12", 3L, 4L,
                PaymentMethod.CASH, "STUDENT"
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(4L)).thenReturn(Optional.of(toStop));
        when(ticketRepo.existsOverlap(1L, "A12", 1, 3)).thenReturn(false);
        when(userRepo.findById(2L)).thenReturn(Optional.of(passenger));
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.HOLD))
                .thenReturn(true);
        when(seatHoldRepo.findByTrip_IdAndPassenger_IdAndStatus(1L, 2L, SeatHoldStatus.HOLD))
                .thenReturn(List.of(seatHold)); // Este pasajero SÍ tiene hold en A12

        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(10L);
            t.setCreatedAt(OffsetDateTime.now());
            t.setStatus(TicketStatus.SOLD);
            return t;
        });
        when(fareRuleRepo.findByRouteIdAndFromStopIdAndToStopId(101L, 3L, 4L))
                .thenReturn(Optional.of(fareRule));

        // When
        var response = service.create(1L, request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A12");
        
        verify(ticketRepo, times(1)).save(any(Ticket.class));
    }

    @Test
    void shouldGetByQRCode() {
        //Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(1L).bus(bus).departureAt(OffsetDateTime.now()).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();
        var fromStop = Stop.builder().id(3L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(4L).name("Medellín").stopOrder(3).build();

        var ticket1 = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();
        ticket1.setQrCode("TKT-H3DY61CKU0JMV9W7");
        // When
        when(ticketRepo.findByQrCode(anyString())).thenReturn(Optional.of(ticket1));

        var result = service.getByQRCode("TKT-H3DY61CKU0JMV9W7");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.seatNumber()).isEqualTo("A12");
        assertThat(result.passenger().userName()).isEqualTo(passenger.getUserName());
    }
}
