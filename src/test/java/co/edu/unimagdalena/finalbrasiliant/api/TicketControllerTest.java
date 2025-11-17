package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import co.edu.unimagdalena.finalbrasiliant.services.TicketService;
import co.edu.unimagdalena.finalbrasiliant.api.dto.TicketDTOs.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    TicketService service;

    @Test
    void createTicket_shouldReturn201AndLocation() throws Exception {
        var req = new TicketCreateRequest(100L, "A12", 1L, 5L, PaymentMethod.CARD, "STUDENT");
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var userSummary = new UserSummary(100L, "John Passenger", "+573001234567");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var resp = new TicketResponse(10L, tripSummary, userSummary, "A12", fromStop, toStop,
                new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CARD,
                TicketStatus.SOLD, "TKT-ABC1234567");

        when(service.create(eq(50L), any())).thenReturn(resp);

        mvc.perform(post("/api/v1/trips/50/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/tickets/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.trip.id").value(50))
                .andExpect(jsonPath("$.passenger.id").value(100))
                .andExpect(jsonPath("$.seatNumber").value("A12"))
                .andExpect(jsonPath("$.status").value("SOLD"))
                .andExpect(jsonPath("$.qrCode").value("TKT-ABC1234567"));

        verify(service).create(eq(50L), any(TicketCreateRequest.class));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var userSummary = new UserSummary(100L, "John Passenger", "+573001234567");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var resp = new TicketResponse(10L, tripSummary, userSummary, "A12", fromStop, toStop,
                new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CARD,
                TicketStatus.SOLD, "TKT-ABC1234567");

        when(service.get(10L)).thenReturn(resp);

        mvc.perform(get("/api/v1/tickets/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("SOLD"));

        verify(service).get(10L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new TicketUpdateRequest("A15", new BigDecimal("50000"), PaymentMethod.CASH, TicketStatus.CANCELLED);
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var userSummary = new UserSummary(100L, "John Passenger", "+573001234567");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var resp = new TicketResponse(10L, tripSummary, userSummary, "A15", fromStop, toStop,
                new BigDecimal("50000"), OffsetDateTime.now(), PaymentMethod.CASH,
                TicketStatus.CANCELLED, null);

        when(service.update(eq(10L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/tickets/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.seatNumber").value("A15"))
                .andExpect(jsonPath("$.price").value(50000));

        verify(service).update(eq(10L), any(TicketUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/tickets/10"))
                .andExpect(status().isNoContent());

        verify(service).delete(10L);
    }

    @Test
    void listByTrip_shouldReturn200() throws Exception {
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var user1 = new UserSummary(100L, "John", "+573001234567");
        var user2 = new UserSummary(101L, "Jane", "+573007654321");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var tickets = List.of(
                new TicketResponse(10L, tripSummary, user1, "A12", fromStop, toStop,
                        new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CARD, TicketStatus.SOLD, null),
                new TicketResponse(11L, tripSummary, user2, "B15", fromStop, toStop,
                        new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CASH, TicketStatus.SOLD, null)
        );

        when(service.listByTrip(50L)).thenReturn(tickets);

        mvc.perform(get("/api/v1/trips/50/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trip.id").value(50))
                .andExpect(jsonPath("$[1].trip.id").value(50))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByTrip(50L);
    }

    @Test
    void getByTripSeat_shouldReturn200() throws Exception {
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var userSummary = new UserSummary(100L, "John Passenger", "+573001234567");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var resp = new TicketResponse(10L, tripSummary, userSummary, "A12", fromStop, toStop,
                new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CARD,
                TicketStatus.SOLD, null);

        when(service.getByTripSeat(50L, "A12")).thenReturn(resp);

        mvc.perform(get("/api/v1/trips/50/tickets/by-seat")
                        .param("seatNumber", "A12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seatNumber").value("A12"))
                .andExpect(jsonPath("$.trip.id").value(50));

        verify(service).getByTripSeat(50L, "A12");
    }

    @Test
    void getByQRCode_shouldReturn200() throws Exception {
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var userSummary = new UserSummary(100L, "John Passenger", "+573001234567");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var resp = new TicketResponse(10L, tripSummary, userSummary, "A12", fromStop, toStop,
                new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CARD,
                TicketStatus.USED, null);

        when(service.getByQRCode("TKT-ABC1234567")).thenReturn(resp);

        mvc.perform(get("/api/v1/tickets/by-qrcode")
                        .param("qrCode", "TKT-ABC1234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("USED"));

        verify(service).getByQRCode("TKT-ABC1234567");
    }

    @Test
    void listByStatus_shouldReturn200() throws Exception {
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var user1 = new UserSummary(100L, "John", "+573001234567");
        var user2 = new UserSummary(101L, "Jane", "+573007654321");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var tickets = List.of(
                new TicketResponse(10L, tripSummary, user1, "A12", fromStop, toStop,
                        new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CARD, TicketStatus.CANCELLED, null),
                new TicketResponse(11L, tripSummary, user2, "B15", fromStop, toStop,
                        new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CASH, TicketStatus.CANCELLED, null)
        );
        var page = new PageImpl<>(tickets, PageRequest.of(0, 10), 2);

        when(service.listByStatus(eq(TicketStatus.CANCELLED), any())).thenReturn(page);

        mvc.perform(get("/api/v1/tickets/by-status")
                        .param("status", "CANCELLED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$.content[1].status").value("CANCELLED"));

        verify(service).listByStatus(eq(TicketStatus.CANCELLED), any());
    }

    @Test
    void listByPaymentMethod_shouldReturn200() throws Exception {
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var user1 = new UserSummary(100L, "John", "+573001234567");
        var user2 = new UserSummary(101L, "Jane", "+573007654321");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var tickets = List.of(
                new TicketResponse(10L, tripSummary, user1, "A12", fromStop, toStop,
                        new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CASH, TicketStatus.SOLD, null),
                new TicketResponse(11L, tripSummary, user2, "B15", fromStop, toStop,
                        new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CASH, TicketStatus.SOLD, null)
        );
        var page = new PageImpl<>(tickets, PageRequest.of(0, 10), 2);

        when(service.listByPaymentMethod(eq(PaymentMethod.CASH), any())).thenReturn(page);

        mvc.perform(get("/api/v1/tickets/by-payment")
                        .param("payment", "CASH")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.content[1].paymentMethod").value("CASH"));

        verify(service).listByPaymentMethod(eq(PaymentMethod.CASH), any());
    }

    @Test
    void listByCreated_shouldReturn200() throws Exception {
        var from = OffsetDateTime.now().minusDays(7);
        var to = OffsetDateTime.now();
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var userSummary = new UserSummary(100L, "John", "+573001234567");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var tickets = List.of(
                new TicketResponse(10L, tripSummary, userSummary, "A12", fromStop, toStop,
                        new BigDecimal("45000"), from.plusDays(1), PaymentMethod.CARD, TicketStatus.SOLD, null)
        );
        var page = new PageImpl<>(tickets, PageRequest.of(0, 10), 1);

        when(service.listByCreatedAt(any(OffsetDateTime.class), any(OffsetDateTime.class), any())).thenReturn(page);

        mvc.perform(get("/api/v1/tickets/by-created")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(10));

        verify(service).listByCreatedAt(any(OffsetDateTime.class), any(OffsetDateTime.class), any());
    }

    @Test
    void listByPassenger_shouldReturn200() throws Exception {
        var tripSummary1 = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var tripSummary2 = new TripSummary(51L, "XYZ789", OffsetDateTime.now().plusDays(2));
        var userSummary = new UserSummary(100L, "John Passenger", "+573001234567");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var tickets = List.of(
                new TicketResponse(10L, tripSummary1, userSummary, "A12", fromStop, toStop,
                        new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CARD, TicketStatus.SOLD, null),
                new TicketResponse(11L, tripSummary2, userSummary, "B20", fromStop, toStop,
                        new BigDecimal("50000"), OffsetDateTime.now(), PaymentMethod.CASH, TicketStatus.USED, null)
        );

        when(service.listByPassenger(100L)).thenReturn(tickets);

        mvc.perform(get("/api/v1/tickets/by-passenger")
                        .param("passengerId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passenger.id").value(100))
                .andExpect(jsonPath("$[1].passenger.id").value(100))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByPassenger(100L);
    }

    @Test
    void listByStretch_withBothStops_shouldReturn200() throws Exception {
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var userSummary = new UserSummary(100L, "John", "+573001234567");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(5L, "Terminal Sur", 5);
        var tickets = List.of(
                new TicketResponse(10L, tripSummary, userSummary, "A12", fromStop, toStop,
                        new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CARD, TicketStatus.SOLD, null)
        );

        when(service.listByStretch(1L, 5L)).thenReturn(tickets);

        mvc.perform(get("/api/v1/tickets/by-stretch")
                        .param("fromStop", "1")
                        .param("toStop", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStop.id").value(1))
                .andExpect(jsonPath("$[0].toStop.id").value(5))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).listByStretch(1L, 5L);
    }

    @Test
    void listByStretch_withOnlyFromStop_shouldReturn200() throws Exception {
        var tripSummary = new TripSummary(50L, "ABC123", OffsetDateTime.now().plusDays(1));
        var userSummary = new UserSummary(100L, "John", "+573001234567");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop1 = new StopSummary(5L, "Terminal Sur", 5);
        var toStop2 = new StopSummary(8L, "Terminal Este", 8);
        var tickets = List.of(
                new TicketResponse(10L, tripSummary, userSummary, "A12", fromStop, toStop1,
                        new BigDecimal("45000"), OffsetDateTime.now(), PaymentMethod.CARD, TicketStatus.SOLD, null),
                new TicketResponse(11L, tripSummary, userSummary, "B15", fromStop, toStop2,
                        new BigDecimal("60000"), OffsetDateTime.now(), PaymentMethod.CASH, TicketStatus.SOLD, null)
        );

        when(service.listByStretch(1L, null)).thenReturn(tickets);

        mvc.perform(get("/api/v1/tickets/by-stretch")
                        .param("fromStop", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStop.id").value(1))
                .andExpect(jsonPath("$[1].fromStop.id").value(1))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByStretch(1L, null);
    }
}