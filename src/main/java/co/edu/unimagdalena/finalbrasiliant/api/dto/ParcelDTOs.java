package co.edu.unimagdalena.finalbrasiliant.api.dto;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.ParcelStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;

public class ParcelDTOs {
    private static final String PHONE_NUMBER_PATTERN = "^(\\+57)?3\\d{9}$";

    public record ParcelCreateRequest(@NotNull Long fromStopId, @NotNull Long toStopId, @NotNull @Positive BigDecimal price,
                                      @NotBlank String senderName, @NotBlank @Pattern(regexp = PHONE_NUMBER_PATTERN) String senderPhone,
                                      @NotBlank String receiverName, @NotBlank @Pattern(regexp = PHONE_NUMBER_PATTERN) String receiverPhone) implements Serializable {}

    public record ParcelUpdateRequest(@Positive BigDecimal price, @NotNull ParcelStatus status,
                                      @Pattern(regexp = PHONE_NUMBER_PATTERN) String senderPhone, String receiverName,
                                      @Pattern(regexp = PHONE_NUMBER_PATTERN) String receiverPhone) implements Serializable {}

    public record ParcelResponse(Long id, String code, StopSummary fromStop, StopSummary toStop, BigDecimal price, ParcelStatus status,
                                 String senderName, String senderPhone, String receiverName, String receiverPhone, String deliveryOTP) implements Serializable {}

    public record StopSummary(Long id, String name, Integer stopOrder) implements Serializable {}
}
