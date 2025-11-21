package co.edu.unimagdalena.finalbrasiliant.api.dto;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentDTOs {
    public record PaymentRequest(@NotNull Long ticketId, @NotNull PaymentMethod paymentMethod,
                                 @NotNull BigDecimal amount){}
    public record PaymentResponse(String id, Long ticketId, PaymentMethod paymentMethod, BigDecimal amount,
                                  OffsetDateTime payedAt, String payedFor){}
}
