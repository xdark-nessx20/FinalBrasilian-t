package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatHoldDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.SeatHold;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatHoldMapperTest {

    private final SeatHoldMapper mapper = Mappers.getMapper(SeatHoldMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        // Given
        var request = new SeatHoldCreateRequest(
                1L,
                "A12",
                2L,
                SeatHoldStatus.HOLD
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getSeatNumber()).isEqualTo("A12");
        assertThat(entity.getStatus()).isEqualTo(SeatHoldStatus.HOLD);

        // Campos ignorados
        assertThat(entity.getId()).isNull();
        assertThat(entity.getTrip()).isNull();
        assertThat(entity.getPassenger()).isNull();
        assertThat(entity.getExpiresAt()).isNull();
    }

    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var trip = Trip.builder()
                .id(1L)
                .departureAt(OffsetDateTime.now().plusDays(1))
                .status(TripStatus.SCHEDULED)
                .build();

        var passenger = User.builder()
                .id(2L)
                .userName("Juan Pérez")
                .build();

        var expiresAt = OffsetDateTime.now().plusMinutes(10);

        var seatHold = SeatHold.builder()
                .id(10L)
                .trip(trip)
                .seatNumber("A12")
                .passenger(passenger)
                .expiresAt(expiresAt)
                .status(SeatHoldStatus.HOLD)
                .build();

        // When
        var response = mapper.toResponse(seatHold);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A12");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.status()).isEqualTo(SeatHoldStatus.HOLD);

        // Trip summary
        assertThat(response.trip().id()).isEqualTo(1L);
        assertThat(response.trip().departureAt()).isNotNull();
        assertThat(response.trip().status()).isEqualTo("SCHEDULED"); // Mapped from enum.name()

        // Passenger summary
        assertThat(response.passenger().id()).isEqualTo(2L);
        assertThat(response.passenger().userName()).isEqualTo("Juan Pérez");
    }

    @Test
    void toUserSummary_shouldMapUser() {
        // Given
        var user = User.builder()
                .id(5L)
                .userName("Ana López")
                .email("ana@example.com")
                .build();

        // When
        var summary = mapper.toUserSummary(user);

        // Then
        assertThat(summary.id()).isEqualTo(5L);
        assertThat(summary.userName()).isEqualTo("Ana López");
    }


    @Test
    void toTripSummary_shouldMapTrip() {
        // Given
        var departureAt = OffsetDateTime.now().plusDays(2);
        var trip = Trip.builder()
                .id(7L)
                .departureAt(departureAt)
                .status(TripStatus.BOARDING)
                .build();

        // When
        var summary = mapper.toTripSummary(trip);

        // Then
        assertThat(summary.id()).isEqualTo(7L);
        assertThat(summary.departureAt()).isEqualTo(departureAt);
        assertThat(summary.status()).isEqualTo("BOARDING");
    }

    @Test
    void patch_shouldUpdateSeatNumberAndStatus() {
        // Given
        var seatHold = SeatHold.builder()
                .id(10L)
                .seatNumber("A12")
                .status(SeatHoldStatus.HOLD)
                .build();

        var updateRequest = new SeatHoldUpdateRequest(
                null,
                "B15",
                null,
                SeatHoldStatus.EXPIRED
        );

        // When
        mapper.patch(seatHold, updateRequest);

        // Then
        assertThat(seatHold.getSeatNumber()).isEqualTo("B15"); // Cambió
        assertThat(seatHold.getStatus()).isEqualTo(SeatHoldStatus.EXPIRED); // Cambió
        assertThat(seatHold.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFields() {
        // Given
        var seatHold = SeatHold.builder()
                .id(10L)
                .seatNumber("A12")
                .status(SeatHoldStatus.HOLD)
                .build();

        var updateRequest = new SeatHoldUpdateRequest(null, null, null, null);

        // When
        mapper.patch(seatHold, updateRequest);

        // Then
        assertThat(seatHold.getSeatNumber()).isEqualTo("A12"); // No cambió
        assertThat(seatHold.getStatus()).isEqualTo(SeatHoldStatus.HOLD); // No cambió
    }

    @Test
    void patch_shouldNotModifyIdOrRelations() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var passenger = User.builder().id(2L).build();
        var expiresAt = OffsetDateTime.now().plusMinutes(10);

        var seatHold = SeatHold.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A12")
                .expiresAt(expiresAt)
                .status(SeatHoldStatus.HOLD)
                .build();

        var updateRequest = new SeatHoldUpdateRequest(99L, "B20", 99L, SeatHoldStatus.EXPIRED);

        // When
        mapper.patch(seatHold, updateRequest);

        // Then
        assertThat(seatHold.getId()).isEqualTo(10L); // No cambió
        assertThat(seatHold.getTrip()).isEqualTo(trip); // No cambió (ignored)
        assertThat(seatHold.getPassenger()).isEqualTo(passenger); // No cambió (ignored)
        assertThat(seatHold.getExpiresAt()).isEqualTo(expiresAt); // No cambió
        assertThat(seatHold.getSeatNumber()).isEqualTo("B20"); // Sí cambió
        assertThat(seatHold.getStatus()).isEqualTo(SeatHoldStatus.EXPIRED); // Sí cambió
    }

}
