package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.PaymentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.services.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest extends BaseTest{
    @MockitoBean
    private PaymentService service;

    @Test
    void confirmPayment_shouldReturn200() throws Exception {
        var req = new PaymentRequest(1L, PaymentMethod.CASH, new BigDecimal("50000"));
        var resp = new PaymentResponse(
                "ABC1234567",
                1L,
                PaymentMethod.CASH,
                new BigDecimal("50000"),
                OffsetDateTime.now(),
                "John Passenger"
        );

        when(service.confirmPayment(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ABC1234567"))
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.amount").value(50000))
                .andExpect(jsonPath("$.payedFor").value("John Passenger"));

        verify(service).confirmPayment(any(PaymentRequest.class));
    }

    @Test
    void confirmPayment_withCreditCard_shouldReturn200() throws Exception {
        var req = new PaymentRequest(2L, PaymentMethod.CARD, new BigDecimal("75000"));
        var resp = new PaymentResponse(
                "XYZ9876543",
                2L,
                PaymentMethod.CARD,
                new BigDecimal("75000"),
                OffsetDateTime.now(),
                "Jane Doe"
        );

        when(service.confirmPayment(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("XYZ9876543"))
                .andExpect(jsonPath("$.ticketId").value(2))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.amount").value(75000))
                .andExpect(jsonPath("$.payedFor").value("Jane Doe"));

        verify(service).confirmPayment(any(PaymentRequest.class));
    }

    @Test
    void confirmPayment_withQR_shouldReturn200() throws Exception {
        var req = new PaymentRequest(3L, PaymentMethod.QR, new BigDecimal("60000"));
        var resp = new PaymentResponse(
                "QR12345678",
                3L,
                PaymentMethod.QR,
                new BigDecimal("60000"),
                OffsetDateTime.now(),
                "Carlos Rodriguez"
        );

        when(service.confirmPayment(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("QR12345678"))
                .andExpect(jsonPath("$.ticketId").value(3))
                .andExpect(jsonPath("$.paymentMethod").value("QR"))
                .andExpect(jsonPath("$.amount").value(60000));

        verify(service).confirmPayment(any(PaymentRequest.class));
    }
}
