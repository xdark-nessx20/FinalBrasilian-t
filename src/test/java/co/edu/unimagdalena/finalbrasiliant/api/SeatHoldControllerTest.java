package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatHoldDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalbrasiliant.services.SeatHoldService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatHoldController.class)
public class SeatHoldControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    SeatHoldService service;

    private TripSummary tripSummary;
    private UserSummary userSummary1;
    private UserSummary userSummary2;
    private OffsetDateTime expiresAt;
    private OffsetDateTime expiredTime;
    private SeatHoldResponse holdResponse1;
    private SeatHoldResponse holdResponse2;

    @BeforeEach
    void setUp() {
        tripSummary = new TripSummary(50L, OffsetDateTime.now().plusDays(1), "SCHEDULED");
        userSummary1 = new UserSummary(100L, "John Passenger");
        userSummary2 = new UserSummary(101L, "Jane Doe");
        expiresAt = OffsetDateTime.now().plusMinutes(10);
        expiredTime = OffsetDateTime.now().minusMinutes(5);

        holdResponse1 = new SeatHoldResponse(1L, tripSummary, "A12", userSummary1, expiresAt, SeatHoldStatus.HOLD);
        holdResponse2 = new SeatHoldResponse(2L, tripSummary, "B15", userSummary2, expiresAt, SeatHoldStatus.HOLD);
    }

    @Test
    void createSeatHold_shouldReturn201AndLocation() throws Exception {
        var req = new SeatHoldCreateRequest(50L, "A12", 100L, SeatHoldStatus.HOLD);

        when(service.create(any())).thenReturn(holdResponse1);

        mvc.perform(post("/api/v1/trips/50/seats/A12/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/seat-holds/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.trip.id").value(50))
                .andExpect(jsonPath("$.seatNumber").value("A12"))
                .andExpect(jsonPath("$.passenger.id").value(100))
                .andExpect(jsonPath("$.passenger.userName").value("John Passenger"))
                .andExpect(jsonPath("$.status").value("HOLD"))
                .andExpect(jsonPath("$.expiresAt").exists());

        verify(service).create(any(SeatHoldCreateRequest.class));
    }

    @Test
    void createSeatHold_withMissingRequiredFields_shouldReturn400() throws Exception {
        var req = new SeatHoldCreateRequest(null, null, null, null);

        mvc.perform(post("/api/v1/trips/50/seats/A12/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void createSeatHold_withBlankSeatNumber_shouldReturn400() throws Exception {
        var req = new SeatHoldCreateRequest(50L, "", 100L, SeatHoldStatus.HOLD);

        mvc.perform(post("/api/v1/trips/50/seats/A12/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void get_shouldReturn200() throws Exception {
        when(service.get(1L)).thenReturn(holdResponse1);

        mvc.perform(get("/api/v1/seat-holds/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.seatNumber").value("A12"))
                .andExpect(jsonPath("$.status").value("HOLD"));

        verify(service).get(1L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new SeatHoldUpdateRequest(null, "B15", null, SeatHoldStatus.SOLD);
        var updated = new SeatHoldResponse(1L, tripSummary, "B15", userSummary1, expiresAt, SeatHoldStatus.SOLD);

        when(service.update(eq(1L), any())).thenReturn(updated);

        mvc.perform(patch("/api/v1/seat-holds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.seatNumber").value("B15"))
                .andExpect(jsonPath("$.status").value("SOLD"));

        verify(service).update(eq(1L), any(SeatHoldUpdateRequest.class));
    }

    @Test
    void update_changingStatus_shouldReturn200() throws Exception {
        var req = new SeatHoldUpdateRequest(null, null, null, SeatHoldStatus.EXPIRED);
        var expired = new SeatHoldResponse(1L, tripSummary, "A12", userSummary1, expiredTime, SeatHoldStatus.EXPIRED);

        when(service.update(eq(1L), any())).thenReturn(expired);

        mvc.perform(patch("/api/v1/seat-holds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));

        verify(service).update(eq(1L), any(SeatHoldUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/seat-holds/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void listByPassenger_shouldReturn200() throws Exception {
        var tripSummary2 = new TripSummary(51L, OffsetDateTime.now().plusDays(2), "SCHEDULED");
        var hold3 = new SeatHoldResponse(3L, tripSummary2, "C20", userSummary1, expiresAt, SeatHoldStatus.HOLD);

        when(service.listByPassenger(100L)).thenReturn(List.of(holdResponse1, hold3));

        mvc.perform(get("/api/v1/seat-holds/by-passenger")
                        .param("passengerId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passenger.id").value(100))
                .andExpect(jsonPath("$[1].passenger.id").value(100))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByPassenger(100L);
    }

    @Test
    void listByTrip_shouldReturn200() throws Exception {
        var sold = new SeatHoldResponse(3L, tripSummary, "C20", userSummary1, expiresAt, SeatHoldStatus.SOLD);

        when(service.listByTrip(50L)).thenReturn(List.of(holdResponse1, holdResponse2, sold));

        mvc.perform(get("/api/v1/trips/50/seat-holds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trip.id").value(50))
                .andExpect(jsonPath("$[1].trip.id").value(50))
                .andExpect(jsonPath("$[2].trip.id").value(50))
                .andExpect(jsonPath("$.length()").value(3));

        verify(service).listByTrip(50L);
    }

    @Test
    void listByStatus_withHOLD_shouldReturn200() throws Exception {
        when(service.listByStatus(SeatHoldStatus.HOLD)).thenReturn(List.of(holdResponse1, holdResponse2));

        mvc.perform(get("/api/v1/seat-holds/by-status")
                        .param("status", "HOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("HOLD"))
                .andExpect(jsonPath("$[1].status").value("HOLD"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByStatus(SeatHoldStatus.HOLD);
    }

    @Test
    void listByStatus_withSOLD_shouldReturn200() throws Exception {
        var sold = new SeatHoldResponse(3L, tripSummary, "C20", userSummary1, expiresAt, SeatHoldStatus.SOLD);

        when(service.listByStatus(SeatHoldStatus.SOLD)).thenReturn(List.of(sold));

        mvc.perform(get("/api/v1/seat-holds/by-status")
                        .param("status", "SOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SOLD"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).listByStatus(SeatHoldStatus.SOLD);
    }

    @Test
    void listByStatus_withEXPIRED_shouldReturn200() throws Exception {
        var expired = new SeatHoldResponse(4L, tripSummary, "D25", userSummary1, expiredTime, SeatHoldStatus.EXPIRED);

        when(service.listByStatus(SeatHoldStatus.EXPIRED)).thenReturn(List.of(expired));

        mvc.perform(get("/api/v1/seat-holds/by-status")
                        .param("status", "EXPIRED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("EXPIRED"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).listByStatus(SeatHoldStatus.EXPIRED);
    }

    @Test
    void listByTripAndPassenger_shouldReturn200() throws Exception {
        when(service.listByTripAndPassenger(50L, 100L, SeatHoldStatus.HOLD)).thenReturn(List.of(holdResponse1));

        mvc.perform(get("/api/v1/seat-holds/by-trip-and-passenger")
                        .param("tripId", "50")
                        .param("passengerId", "100")
                        .param("status", "HOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trip.id").value(50))
                .andExpect(jsonPath("$[0].passenger.id").value(100))
                .andExpect(jsonPath("$[0].status").value("HOLD"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).listByTripAndPassenger(50L, 100L, SeatHoldStatus.HOLD);
    }

    @Test
    void listByTripAndPassenger_withMultipleSeats_shouldReturn200() throws Exception {
        var sold1 = new SeatHoldResponse(1L, tripSummary, "A12", userSummary1, expiresAt, SeatHoldStatus.SOLD);
        var sold2 = new SeatHoldResponse(2L, tripSummary, "A13", userSummary1, expiresAt, SeatHoldStatus.SOLD);

        when(service.listByTripAndPassenger(50L, 100L, SeatHoldStatus.SOLD)).thenReturn(List.of(sold1, sold2));

        mvc.perform(get("/api/v1/seat-holds/by-trip-and-passenger")
                        .param("tripId", "50")
                        .param("passengerId", "100")
                        .param("status", "SOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value("A12"))
                .andExpect(jsonPath("$[1].seatNumber").value("A13"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByTripAndPassenger(50L, 100L, SeatHoldStatus.SOLD);
    }

    @Test
    void listExpiredHolds_shouldReturn200() throws Exception {
        var expired1 = new SeatHoldResponse(1L, tripSummary, "A12", userSummary1, expiredTime, SeatHoldStatus.EXPIRED);
        var expired2 = new SeatHoldResponse(2L, tripSummary, "B15", userSummary2, expiredTime, SeatHoldStatus.EXPIRED);

        when(service.listExpiredHolds()).thenReturn(List.of(expired1, expired2));

        mvc.perform(get("/api/v1/seat-holds/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("EXPIRED"))
                .andExpect(jsonPath("$[1].status").value("EXPIRED"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listExpiredHolds();
    }

    @Test
    void listExpiredHolds_withNoExpired_shouldReturn200WithEmptyList() throws Exception {
        when(service.listExpiredHolds()).thenReturn(List.of());

        mvc.perform(get("/api/v1/seat-holds/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service).listExpiredHolds();
    }

    @Test
    void createSeatHold_forDifferentTrips_shouldReturn201() throws Exception {
        var req = new SeatHoldCreateRequest(51L, "A01", 100L, SeatHoldStatus.HOLD);
        var tripSummary2 = new TripSummary(51L, OffsetDateTime.now().plusDays(2), "SCHEDULED");
        var resp = new SeatHoldResponse(5L, tripSummary2, "A01", userSummary1, expiresAt, SeatHoldStatus.HOLD);

        when(service.create(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/trips/51/seats/A01/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trip.id").value(51))
                .andExpect(jsonPath("$.seatNumber").value("A01"));

        verify(service).create(any(SeatHoldCreateRequest.class));
    }

}
