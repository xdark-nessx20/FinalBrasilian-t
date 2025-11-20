package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import co.edu.unimagdalena.finalbrasiliant.services.SeatService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
class SeatControllerTest extends BaseTest {
    
    @MockitoBean
    SeatService service;

    @Test
    void createSeat_shouldReturn201AndLocation() throws Exception {
        var req = new SeatCreateRequest(100L, "A12", SeatType.STANDARD);
        var resp = new SeatResponse(1L, 100L, "A12", SeatType.STANDARD);

        when(service.create(eq(100L), any(SeatCreateRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/buses/100/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/buses/100/seats/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bus_id").value(100))
                .andExpect(jsonPath("$.number").value("A12"))
                .andExpect(jsonPath("$.type").value("STANDARD"));

        verify(service).create(eq(100L), any(SeatCreateRequest.class));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new SeatResponse(1L, 100L, "A12", SeatType.STANDARD);

        when(service.get(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/seats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bus_id").value(100))
                .andExpect(jsonPath("$.number").value("A12"))
                .andExpect(jsonPath("$.type").value("STANDARD"));

        verify(service).get(1L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new SeatUpdateRequest(100L, "B15", SeatType.PREFERENTIAL);
        var resp = new SeatResponse(1L, 100L, "B15", SeatType.PREFERENTIAL);

        when(service.update(eq(1L), any(SeatUpdateRequest.class))).thenReturn(resp);

        mvc.perform(patch("/api/v1/seats/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bus_id").value(100))
                .andExpect(jsonPath("$.number").value("B15"))
                .andExpect(jsonPath("$.type").value("PREFERENTIAL"));

        verify(service).update(eq(1L), any(SeatUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/seats/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void getByBus_shouldReturn200() throws Exception {
        var seats = List.of(
                new SeatResponse(1L, 100L, "A12", SeatType.STANDARD),
                new SeatResponse(2L, 100L, "A13", SeatType.STANDARD),
                new SeatResponse(3L, 100L, "B01", SeatType.PREFERENTIAL)
        );

        when(service.getSeatsByBus(100L)).thenReturn(seats);

        mvc.perform(get("/api/v1/buses/100/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bus_id").value(100))
                .andExpect(jsonPath("$[1].bus_id").value(100))
                .andExpect(jsonPath("$[2].bus_id").value(100))
                .andExpect(jsonPath("$.length()").value(3));

        verify(service).getSeatsByBus(100L);
    }

    @Test
    void getSeatByNumberAndBus_shouldReturn200() throws Exception {
        var resp = new SeatResponse(1L, 100L, "A12", SeatType.STANDARD);

        when(service.getSeatByNumberAndBus("A12", 100L)).thenReturn(resp);

        mvc.perform(get("/api/v1/buses/100/seats/by-number")
                        .param("number", "A12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bus_id").value(100))
                .andExpect(jsonPath("$.number").value("A12"));

        verify(service).getSeatByNumberAndBus("A12", 100L);
    }

    @Test
    void getByType_shouldReturn200() throws Exception {
        var seats = List.of(
                new SeatResponse(1L, 100L, "B01", SeatType.PREFERENTIAL),
                new SeatResponse(2L, 100L, "B02", SeatType.PREFERENTIAL),
                new SeatResponse(3L, 101L, "C01", SeatType.PREFERENTIAL)
        );

        when(service.getSeatsByType(SeatType.PREFERENTIAL)).thenReturn(seats);

        mvc.perform(get("/api/v1/seats/by-tipe")
                        .param("type", "PREFERENTIAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("PREFERENTIAL"))
                .andExpect(jsonPath("$[1].type").value("PREFERENTIAL"))
                .andExpect(jsonPath("$[2].type").value("PREFERENTIAL"))
                .andExpect(jsonPath("$.length()").value(3));

        verify(service).getSeatsByType(SeatType.PREFERENTIAL);
    }
}