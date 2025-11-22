package co.edu.unimagdalena.finalbrasiliant.api.dto;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class SeatHoldDTOs {

    public record SeatHoldCreateRequest(@NotBlank String seatNumber, @NotNull Long passengerId) implements Serializable {}

    public record SeatHoldUpdateRequest(Long tripId, String seatNumber,
                                        Long passengerId, SeatHoldStatus status) implements Serializable {}

    public record SeatHoldResponse(Long id, TripSummary trip, String seatNumber,
                                    UserSummary passenger,
                                    OffsetDateTime expiresAt,
                                    SeatHoldStatus status) implements Serializable {}

    public record UserSummary(Long id, String userName) implements Serializable {}
    public record TripSummary(Long id, OffsetDateTime departureAt, String status) implements Serializable {}

}
