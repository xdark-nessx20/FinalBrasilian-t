package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.services.BaggageService;
import co.edu.unimagdalena.finalbrasiliant.api.dto.BaggageDTOs.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BaggageController.class)
class BaggageControllerTest extends BaseTest{
    @MockitoBean
    BaggageService service;

    @Test
    void createBaggage_shouldReturn201AndLocation() throws Exception {
        var req = new BaggageCreateRequest(new BigDecimal("25.5"), new BigDecimal("10.00"));
        var ticketSummary = new TicketSummary(100L, "John Passenger");
        var resp = new BaggageResponse(15L, ticketSummary, new BigDecimal("25.5"), new BigDecimal("10.00"), "BAG-ABC12345");

        when(service.create(eq(100L), any())).thenReturn(resp);

        mvc.perform(post("/api/v1/tickets/100/baggages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/baggages/15")))
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.ticket.id").value(100))
                .andExpect(jsonPath("$.weightKg").value(25.5))
                .andExpect(jsonPath("$.fee").value(10.00))
                .andExpect(jsonPath("$.tagCode").value("BAG-ABC12345"));

        verify(service).create(eq(100L), any(BaggageCreateRequest.class));
    }

    @Test
    void listByTicket_shouldReturn200() throws Exception {
        var ticketSummary = new TicketSummary(100L, "John Passenger");
        var baggages = List.of(
                new BaggageResponse(1L, ticketSummary, new BigDecimal("20.0"), new BigDecimal("0.00"), "BAG-TAG001"),
                new BaggageResponse(2L, ticketSummary, new BigDecimal("15.5"), new BigDecimal("5.00"), "BAG-TAG002")
        );

        when(service.listByTicket(100L)).thenReturn(baggages);

        mvc.perform(get("/api/v1/tickets/100/baggages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByTicket(100L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var ticketSummary = new TicketSummary(100L, "John Passenger");
        var resp = new BaggageResponse(15L, ticketSummary, new BigDecimal("25.5"), new BigDecimal("10.00"), "BAG-ABC12345");

        when(service.get(15L)).thenReturn(resp);

        mvc.perform(get("/api/v1/baggages/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.tagCode").value("BAG-ABC12345"));

        verify(service).get(15L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new BaggageUpdateRequest(new BigDecimal("15.00"));
        var ticketSummary = new TicketSummary(100L, "John Passenger");
        var resp = new BaggageResponse(15L, ticketSummary, new BigDecimal("25.5"), new BigDecimal("15.00"), "BAG-ABC12345");

        when(service.update(eq(15L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/baggages/15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.fee").value(15.00));

        verify(service).update(eq(15L), any(BaggageUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/baggages/15"))
                .andExpect(status().isNoContent());

        verify(service).delete(15L);
    }

    @Test
    void getByTagCode_shouldReturn200() throws Exception {
        var ticketSummary = new TicketSummary(100L, "John Passenger");
        var resp = new BaggageResponse(15L, ticketSummary, new BigDecimal("25.5"), new BigDecimal("10.00"), "BAG-ABC12345");

        when(service.getByTagCode("BAG-ABC12345")).thenReturn(resp);

        mvc.perform(get("/api/v1/baggages/by-tag")
                        .param("tagCode", "BAG-ABC12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.tagCode").value("BAG-ABC12345"));

        verify(service).getByTagCode("BAG-ABC12345");
    }

    @Test
    void listByPassenger_shouldReturn200() throws Exception {
        var ticketSummary1 = new TicketSummary(100L, "John Passenger");
        var ticketSummary2 = new TicketSummary(101L, "John Passenger");
        var baggages = List.of(
                new BaggageResponse(1L, ticketSummary1, new BigDecimal("20.0"), new BigDecimal("0.00"), "BAG-TAG001"),
                new BaggageResponse(2L, ticketSummary2, new BigDecimal("18.5"), new BigDecimal("5.00"), "BAG-TAG002")
        );

        when(service.listByPassenger(50L)).thenReturn(baggages);

        mvc.perform(get("/api/v1/baggages/by-passenger")
                        .param("passengerId", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByPassenger(50L);
    }

    @Test
    void listByWeight_greaterThanEq_shouldReturn200() throws Exception {
        var ticketSummary = new TicketSummary(100L, "John Passenger");
        var baggages = List.of(
                new BaggageResponse(1L, ticketSummary, new BigDecimal("30.0"), new BigDecimal("10.00"), "BAG-TAG001"),
                new BaggageResponse(2L, ticketSummary, new BigDecimal("25.0"), new BigDecimal("5.00"), "BAG-TAG002")
        );
        var page = new PageImpl<>(baggages, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "weightKg")), 2);

        when(service.listByWeightKg(any(BigDecimal.class), eq(true), any())).thenReturn(page);

        mvc.perform(get("/api/v1/baggages/by-weight")
                        .param("weightKg", "25.0")
                        .param("gtrThanEq", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].weightKg").value(30.0))
                .andExpect(jsonPath("$.content[1].weightKg").value(25.0));

        verify(service).listByWeightKg(any(BigDecimal.class), eq(true), any());
    }

    @Test
    void listByWeight_lessThanEq_shouldReturn200() throws Exception {
        var ticketSummary = new TicketSummary(100L, "John Passenger");
        var baggages = List.of(
                new BaggageResponse(3L, ticketSummary, new BigDecimal("15.0"), new BigDecimal("0.00"), "BAG-TAG003")
        );
        var page = new PageImpl<>(baggages, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "weightKg")), 1);

        when(service.listByWeightKg(any(BigDecimal.class), eq(false), any())).thenReturn(page);

        mvc.perform(get("/api/v1/baggages/by-weight")
                        .param("weightKg", "20.0")
                        .param("gtrThanEq", "false")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].weightKg").value(15.0));

        verify(service).listByWeightKg(any(BigDecimal.class), eq(false), any());
    }
}