package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TicketDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
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

    @Spy
    private TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @InjectMocks
    private TicketServiceImpl service;

    @Test
    void shouldCreateTicketSuccessfully() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var fromStop = Stop.builder().id(3L).stopOrder(1).build();
        var toStop = Stop.builder().id(4L).stopOrder(3).build();
        var passenger = User.builder().id(2L).userName("Juan").phone("3001234567").build();

        var request = new TicketCreateRequest(
                1L, 2L, "A12", 3L, 4L,
                new BigDecimal("50000.00"),
                PaymentMethod.CARD
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(4L)).thenReturn(Optional.of(toStop));
        when(ticketRepo.existsOverlap(1L, "A12", 1, 3)).thenReturn(false);
        when(userRepo.findById(2L)).thenReturn(Optional.of(passenger));
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.HOLD))
                .thenReturn(false);

        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(10L);
            t.setCreatedAt(OffsetDateTime.now());
            t.setStatus(TicketStatus.SOLD);
            return t;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A12");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(response.passenger().id()).isEqualTo(2L);

        verify(ticketRepo, times(2)).save(any(Ticket.class)); // save() se llama 2 veces (crear + QR)
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripNotExists() {
        // Given
        var request = new TicketCreateRequest(
                99L, 2L, "A12", 3L, 4L,
                new BigDecimal("50000.00"),
                PaymentMethod.CARD
        );

        when(tripRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");

        verify(ticketRepo, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFromStopNotExists() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var request = new TicketCreateRequest(
                1L, 2L, "A12", 99L, 4L,
                new BigDecimal("50000.00"),
                PaymentMethod.CARD
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
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
                1L, 2L, "A12", 3L, 99L,
                new BigDecimal("50000.00"),
                PaymentMethod.CARD
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
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
                1L, 2L, "A12", 3L, 4L,
                new BigDecimal("50000.00"),
                PaymentMethod.CARD
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(4L)).thenReturn(Optional.of(toStop));
        when(ticketRepo.existsOverlap(1L, "A12", 1, 3)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
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
                1L, 99L, "A12", 3L, 4L,
                new BigDecimal("50000.00"),
                PaymentMethod.CARD
        );

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepo.findById(3L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(4L)).thenReturn(Optional.of(toStop));
        when(ticketRepo.existsOverlap(1L, "A12", 1, 3)).thenReturn(false);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
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
                1L, 2L, "A12", 3L, 4L,
                new BigDecimal("50000.00"),
                PaymentMethod.CARD
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
        assertThatThrownBy(() -> service.create(request))
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
                .build();

        var updateRequest = new TicketUpdateRequest(
                "B15",
                new BigDecimal("60000.00"),
                PaymentMethod.CARD,
                TicketStatus.CANCELLED
        );

        when(ticketRepo.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

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
        when(ticketRepo.findById(99L)).thenReturn(Optional.empty());

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
}