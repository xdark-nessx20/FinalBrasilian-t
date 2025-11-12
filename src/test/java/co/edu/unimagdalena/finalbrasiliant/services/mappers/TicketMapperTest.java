package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TicketDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TicketMapperTest {

    private final TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @Test
    void toEntity_shouldMapOnlyAllowedFields() {
        // Given
        var request = new TicketCreateRequest(
                1L,
                2L,
                "A12",
                3L,
                4L,
                new BigDecimal("50000.00"),
                PaymentMethod.CARD
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getSeatNumber()).isEqualTo("A12");
        assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(entity.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);

        // Campos ignorados
        assertThat(entity.getId()).isNull();
        assertThat(entity.getTrip()).isNull();
        assertThat(entity.getPassenger()).isNull();
        assertThat(entity.getFromStop()).isNull();
        assertThat(entity.getToStop()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getQrCode()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder()
                .id(1L)
                .bus(bus)
                .departureAt(OffsetDateTime.now().plusDays(1))
                .build();

        var passenger = User.builder()
                .id(2L)
                .userName("Juan Pérez")
                .phone("3001234567")
                .build();

        var fromStop = Stop.builder()
                .id(3L)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .build();

        var toStop = Stop.builder()
                .id(4L)
                .name("Terminal Medellín")
                .stopOrder(3)
                .build();

        var createdAt = OffsetDateTime.now();

        var ticket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .createdAt(createdAt)
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .build();

        // When
        var response = mapper.toResponse(ticket);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A12");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(response.status()).isEqualTo(TicketStatus.SOLD);

        // Trip summary
        assertThat(response.trip().id()).isEqualTo(1L);
        assertThat(response.trip().busPlate()).isEqualTo("ABC123");
        assertThat(response.trip().departureAt()).isNotNull();

        // Passenger summary
        assertThat(response.passenger().id()).isEqualTo(2L);
        assertThat(response.passenger().userName()).isEqualTo("Juan Pérez");
        assertThat(response.passenger().phone()).isEqualTo("3001234567");

        // Stops summary
        assertThat(response.fromStop().id()).isEqualTo(3L);
        assertThat(response.fromStop().name()).isEqualTo("Terminal Bogotá");
        assertThat(response.fromStop().stopOrder()).isEqualTo(1);

        assertThat(response.toStop().id()).isEqualTo(4L);
        assertThat(response.toStop().name()).isEqualTo("Terminal Medellín");
        assertThat(response.toStop().stopOrder()).isEqualTo(3);
    }

    @Test
    void toUserSummary_shouldMapUser() {
        // Given
        var user = User.builder()
                .id(5L)
                .userName("Ana López")
                .phone("3009876543")
                .email("ana@example.com")
                .build();

        // When
        var summary = mapper.toUserSummary(user);

        // Then
        assertThat(summary.id()).isEqualTo(5L);
        assertThat(summary.userName()).isEqualTo("Ana López");
        assertThat(summary.phone()).isEqualTo("3009876543");
    }

    @Test
    void toUserSummary_shouldHandleNull() {
        // When
        var summary = mapper.toUserSummary(null);

        // Then
        assertThat(summary).isNull();
    }

    @Test
    void toStopSummary_shouldMapStop() {
        // Given
        var stop = Stop.builder()
                .id(10L)
                .name("Peaje La Línea")
                .stopOrder(2)
                .build();

        // When
        var summary = mapper.toStopSummary(stop);

        // Then
        assertThat(summary.id()).isEqualTo(10L);
        assertThat(summary.name()).isEqualTo("Peaje La Línea");
        assertThat(summary.stopOrder()).isEqualTo(2);
    }

    @Test
    void toStopSummary_shouldHandleNull() {
        // When
        var summary = mapper.toStopSummary(null);

        // Then
        assertThat(summary).isNull();
    }

    @Test
    void toTripSummary_shouldMapTripWithBus() {
        // Given
        var bus = Bus.builder().id(1L).plate("XYZ789").build();
        var trip = Trip.builder()
                .id(20L)
                .bus(bus)
                .departureAt(OffsetDateTime.now().plusDays(2))
                .build();

        // When
        var summary = mapper.toTripSummary(trip);

        // Then
        assertThat(summary.id()).isEqualTo(20L);
        assertThat(summary.busPlate()).isEqualTo("XYZ789");
        assertThat(summary.departureAt()).isNotNull();
    }

    @Test
    void toTripSummary_shouldHandleNull() {
        // When
        var summary = mapper.toTripSummary(null);

        // Then
        assertThat(summary).isNull();
    }

    @Test
    void patch_shouldUpdateAllProvidedFields() {
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

        // When
        mapper.patch(ticket, updateRequest);

        // Then
        assertThat(ticket.getSeatNumber()).isEqualTo("B15");
        assertThat(ticket.getPrice()).isEqualByComparingTo(new BigDecimal("60000.00"));
        assertThat(ticket.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(ticket.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFields() {
        // Given
        var ticket = Ticket.builder()
                .id(10L)
                .seatNumber("A12")
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .build();

        var updateRequest = new TicketUpdateRequest(null, null, null, null);

        // When
        mapper.patch(ticket, updateRequest);

        // Then
        assertThat(ticket.getSeatNumber()).isEqualTo("A12"); // No cambió
        assertThat(ticket.getPrice()).isEqualByComparingTo(new BigDecimal("50000.00")); // No cambió
        assertThat(ticket.getPaymentMethod()).isEqualTo(PaymentMethod.CASH); // No cambió
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.SOLD); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlySeatNumber() {
        // Given
        var ticket = Ticket.builder()
                .id(10L)
                .seatNumber("A12")
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .build();

        var updateRequest = new TicketUpdateRequest("C20", null, null, null);

        // When
        mapper.patch(ticket, updateRequest);

        // Then
        assertThat(ticket.getSeatNumber()).isEqualTo("C20"); // Cambió
        assertThat(ticket.getPrice()).isEqualByComparingTo(new BigDecimal("50000.00")); // No cambió
        assertThat(ticket.getPaymentMethod()).isEqualTo(PaymentMethod.CASH); // No cambió
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.SOLD); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyStatus() {
        // Given
        var ticket = Ticket.builder()
                .id(10L)
                .seatNumber("A12")
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .build();

        var updateRequest = new TicketUpdateRequest(null, null, null, TicketStatus.NO_SHOW);

        // When
        mapper.patch(ticket, updateRequest);

        // Then
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.NO_SHOW); // Cambió
        assertThat(ticket.getSeatNumber()).isEqualTo("A12"); // No cambió
        assertThat(ticket.getPrice()).isEqualByComparingTo(new BigDecimal("50000.00")); // No cambió
        assertThat(ticket.getPaymentMethod()).isEqualTo(PaymentMethod.CASH); // No cambió
    }

    @Test
    void patch_shouldNotModifyIdOrRelations() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var passenger = User.builder().id(2L).build();

        var ticket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .build();

        var updateRequest = new TicketUpdateRequest(
                "B20",
                new BigDecimal("70000.00"),
                PaymentMethod.CARD,
                TicketStatus.CANCELLED
        );

        // When
        mapper.patch(ticket, updateRequest);

        // Then
        assertThat(ticket.getId()).isEqualTo(10L); // No cambió
        assertThat(ticket.getTrip()).isEqualTo(trip); // No cambió
        assertThat(ticket.getPassenger()).isEqualTo(passenger); // No cambió
    }
}