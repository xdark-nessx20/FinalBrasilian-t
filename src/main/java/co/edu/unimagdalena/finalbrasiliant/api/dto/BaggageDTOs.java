package co.edu.unimagdalena.finalbrasiliant.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

public class BaggageDTOs {
    public record BaggageCreateRequest(@NotNull @Positive BigDecimal weightKg,
                                       @NotNull @PositiveOrZero BigDecimal fee) implements Serializable {}

    public record BaggageUpdateRequest(@PositiveOrZero BigDecimal fee) implements Serializable {}

    public record BaggageResponse(Long id, TicketSummary ticket, BigDecimal weightKg, BigDecimal fee, String tagCode) implements Serializable {}

    public record TicketSummary(Long id, String passengerName) implements Serializable {}
}
