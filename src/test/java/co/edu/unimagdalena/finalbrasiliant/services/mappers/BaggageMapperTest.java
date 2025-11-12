package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BaggageDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Baggage;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Ticket;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BaggageMapperTest {

    private final BaggageMapper mapper = Mappers.getMapper(BaggageMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        // Given
        var request = new BaggageCreateRequest(
                new BigDecimal("15.50"),
                new BigDecimal("25000.00")
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getWeightKg()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(entity.getFee()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(entity.getId()).isNull(); // Ignored
        assertThat(entity.getTicket()).isNull(); // Ignored
        assertThat(entity.getTagCode()).isNull(); // Ignored
    }

    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var passenger = User.builder()
                .id(1L)
                .userName("Ana López")
                .email("ana@example.com")
                .build();

        var ticket = Ticket.builder()
                .id(5L)
                .passenger(passenger)
                .seatNumber("A12")
                .build();

        var baggage = Baggage.builder()
                .id(10L)
                .ticket(ticket)
                .weightKg(new BigDecimal("20.00"))
                .fee(new BigDecimal("30000.00"))
                .tagCode("BAG-ABC12345")
                .build();

        // When
        var response = mapper.toResponse(baggage);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.ticket().id()).isEqualTo(5L);
        assertThat(response.ticket().passengerName()).isEqualTo("Ana López");
        assertThat(response.weightKg()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(response.tagCode()).isEqualTo("BAG-ABC12345");
    }

    @Test
    void toTicketSummary_shouldMapTicket() {
        // Given
        var passenger = User.builder()
                .id(1L)
                .userName("Carlos Ruiz")
                .email("carlos@example.com")
                .build();

        var ticket = Ticket.builder()
                .id(7L)
                .passenger(passenger)
                .seatNumber("B15")
                .build();

        // When
        var ticketSummary = mapper.toTicketSummary(ticket);

        // Then
        assertThat(ticketSummary.id()).isEqualTo(7L);
        assertThat(ticketSummary.passengerName()).isEqualTo("Carlos Ruiz");
    }

    @Test
    void toTicketSummary_shouldHandleNullTicket() {
        // When
        var ticketSummary = mapper.toTicketSummary(null);

        // Then
        assertThat(ticketSummary).isNull();
    }

    @Test
    void patch_shouldUpdateFee() {
        // Given
        var baggage = Baggage.builder()
                .id(10L)
                .weightKg(new BigDecimal("15.50"))
                .fee(new BigDecimal("25000.00"))
                .tagCode("BAG-ABC12345")
                .build();

        var updateRequest = new BaggageUpdateRequest(new BigDecimal("30000.00"));

        // When
        mapper.patch(baggage, updateRequest);

        // Then
        assertThat(baggage.getFee()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(baggage.getWeightKg()).isEqualByComparingTo(new BigDecimal("15.50")); // No cambió
        assertThat(baggage.getTagCode()).isEqualTo("BAG-ABC12345"); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFee() {
        // Given
        var baggage = Baggage.builder()
                .id(10L)
                .weightKg(new BigDecimal("15.50"))
                .fee(new BigDecimal("25000.00"))
                .tagCode("BAG-ABC12345")
                .build();

        var updateRequest = new BaggageUpdateRequest(null);

        // When
        mapper.patch(baggage, updateRequest);

        // Then
        assertThat(baggage.getFee()).isEqualByComparingTo(new BigDecimal("25000.00")); // No cambió
    }

    @Test
    void patch_shouldUpdateFeeToZero() {
        // Given
        var baggage = Baggage.builder()
                .id(10L)
                .weightKg(new BigDecimal("15.50"))
                .fee(new BigDecimal("25000.00"))
                .tagCode("BAG-ABC12345")
                .build();

        var updateRequest = new BaggageUpdateRequest(BigDecimal.ZERO);

        // When
        mapper.patch(baggage, updateRequest);

        // Then
        assertThat(baggage.getFee()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void toResponse_shouldHandleNullTicket() {
        // Given
        var baggage = Baggage.builder()
                .id(10L)
                .ticket(null)
                .weightKg(new BigDecimal("20.00"))
                .fee(new BigDecimal("30000.00"))
                .tagCode("BAG-ABC12345")
                .build();

        // When
        var response = mapper.toResponse(baggage);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.ticket()).isNull();
        assertThat(response.weightKg()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(response.tagCode()).isEqualTo("BAG-ABC12345");
    }

    @Test
    void toTicketSummary_shouldMapNestedPassengerName() {
        // Given
        var passenger = User.builder()
                .id(3L)
                .userName("María García")
                .email("maria@example.com")
                .phone("3009876543")
                .build();

        var ticket = Ticket.builder()
                .id(15L)
                .passenger(passenger)
                .seatNumber("C20")
                .build();

        // When
        var ticketSummary = mapper.toTicketSummary(ticket);

        // Then
        assertThat(ticketSummary.id()).isEqualTo(15L);
        assertThat(ticketSummary.passengerName()).isEqualTo("María García");
    }
}