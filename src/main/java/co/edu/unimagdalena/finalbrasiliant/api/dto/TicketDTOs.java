package co.edu.unimagdalena.finalbrasiliant.api.dto;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TicketDTOs {
    public record TicketCreateRequest(@NotNull Long passengerId, @NotNull String seatNumber,
                                      @NotNull Long fromStopId, @NotNull Long toStopId, @NotNull @Positive BigDecimal price,
                                      @NotNull PaymentMethod paymentMethod) implements Serializable {}

    public record TicketUpdateRequest(String seatNumber, @Positive BigDecimal price, PaymentMethod paymentMethod,
                                      TicketStatus status) implements Serializable {}

    public record TicketResponse(Long id, TripSummary trip, UserSummary passenger, String seatNumber, StopSummary fromStop,
                                 StopSummary toStop, BigDecimal price, OffsetDateTime createdAt, PaymentMethod paymentMethod,
                                 TicketStatus status) implements Serializable {}

    public record TripSummary(Long id, String busPlate, OffsetDateTime departureAt) implements Serializable {}
    public record UserSummary(Long id, String userName, String phone) implements Serializable {}
    public record StopSummary(Long id, String name, Integer stopOrder) implements Serializable {}
}
