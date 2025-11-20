package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import co.edu.unimagdalena.finalbrasiliant.services.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
public class TripControllerTest extends BaseTest {
    
    @MockitoBean
    TripService service;

    @Test
    void createTrip_shouldReturn201AndLocation() throws Exception {
        var date = LocalDate.of(2025, 12, 1);
        var departure = OffsetDateTime.now().plusDays(1);
        var arrival = departure.plusHours(2);
        var req = new TripCreateRequest(50L, date, departure, arrival);
        var resp = new TripResponse(1L, 10L, 50L, date, departure, arrival, TripStatus.SCHEDULED);

        when(service.create(eq(10L), any(TripCreateRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/routes/10/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/routes/10/trips/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.route_id").value(10))
                .andExpect(jsonPath("$.bus_id").value(50))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(service).create(eq(10L), any(TripCreateRequest.class));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var date = LocalDate.of(2025, 12, 1);
        var departure = OffsetDateTime.now().plusDays(1);
        var arrival = departure.plusHours(2);
        var resp = new TripResponse(1L, 10L, 50L, date, departure, arrival, TripStatus.SCHEDULED);

        when(service.get(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.route_id").value(10))
                .andExpect(jsonPath("$.bus_id").value(50))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(service).get(1L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var date = LocalDate.of(2025, 12, 1);
        var departure = OffsetDateTime.now().plusDays(1);
        var arrival = departure.plusHours(2);
        var req = new TripUpdateRequest(10L, 50L, date, departure, arrival, TripStatus.DEPARTED);
        var resp = new TripResponse(1L, 10L, 50L, date, departure, arrival, TripStatus.DEPARTED);

        when(service.update(eq(1L), any(TripUpdateRequest.class))).thenReturn(resp);

        mvc.perform(patch("/api/v1/trips/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DEPARTED"));

        verify(service).update(eq(1L), any(TripUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/trips/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void getByRouteId_shouldReturn200() throws Exception {
        var date1 = LocalDate.of(2025, 12, 1);
        var date2 = LocalDate.of(2025, 12, 2);
        var departure1 = OffsetDateTime.now().plusDays(1);
        var departure2 = OffsetDateTime.now().plusDays(2);
        var trips = List.of(
                new TripResponse(1L, 10L, 50L, date1, departure1, departure1.plusHours(2), TripStatus.SCHEDULED),
                new TripResponse(2L, 10L, 51L, date2, departure2, departure2.plusHours(2), TripStatus.SCHEDULED)
        );
        var page = new PageImpl<>(trips, PageRequest.of(0, 10), 2);

        when(service.getAllByRouteId(eq(10L), any())).thenReturn(page);

        mvc.perform(get("/api/v1/routes/10/trips")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].route_id").value(10))
                .andExpect(jsonPath("$.content[1].route_id").value(10));

        verify(service).getAllByRouteId(eq(10L), any());
    }

    @Test
    void getByBusId_shouldReturn200() throws Exception {
        var date1 = LocalDate.of(2025, 12, 1);
        var date2 = LocalDate.of(2025, 12, 3);
        var departure1 = OffsetDateTime.now().plusDays(1);
        var departure2 = OffsetDateTime.now().plusDays(3);
        var trips = List.of(
                new TripResponse(1L, 10L, 50L, date1, departure1, departure1.plusHours(2), TripStatus.SCHEDULED),
                new TripResponse(3L, 11L, 50L, date2, departure2, departure2.plusHours(3), TripStatus.SCHEDULED)
        );
        var page = new PageImpl<>(trips, PageRequest.of(0, 10), 2);

        when(service.getAllByBusId(eq(50L), any())).thenReturn(page);

        mvc.perform(get("/api/v1/trips/by-bus")
                        .param("busId", "50")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].bus_id").value(50))
                .andExpect(jsonPath("$.content[1].bus_id").value(50));

        verify(service).getAllByBusId(eq(50L), any());
    }

    @Test
    void getByDepartureBetween_shouldReturn200() throws Exception {
        var start = OffsetDateTime.now();
        var end = start.plusDays(7);
        var date = LocalDate.of(2025, 12, 1);
        var departure = start.plusDays(1);
        var trips = List.of(
                new TripResponse(1L, 10L, 50L, date, departure, departure.plusHours(2), TripStatus.SCHEDULED)
        );
        var page = new PageImpl<>(trips, PageRequest.of(0, 10), 1);

        when(service.getByDepartureBetween(any(OffsetDateTime.class), any(OffsetDateTime.class), any()))
                .thenReturn(page);

        mvc.perform(get("/api/v1/trips/by-departure")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(service).getByDepartureBetween(any(OffsetDateTime.class), any(OffsetDateTime.class), any());
    }

    @Test
    void getByArrivalBetween_shouldReturn200() throws Exception {
        var start = OffsetDateTime.now().plusHours(2);
        var end = start.plusDays(7);
        var date = LocalDate.of(2025, 12, 1);
        var departure = OffsetDateTime.now().plusDays(1);
        var arrival = departure.plusHours(2);
        var trips = List.of(
                new TripResponse(1L, 10L, 50L, date, departure, arrival, TripStatus.SCHEDULED)
        );
        var page = new PageImpl<>(trips, PageRequest.of(0, 10), 1);

        when(service.getByArrivalBetween(any(OffsetDateTime.class), any(OffsetDateTime.class), any()))
                .thenReturn(page);

        mvc.perform(get("/api/v1/trips/by-arrival")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(service).getByArrivalBetween(any(OffsetDateTime.class), any(OffsetDateTime.class), any());
    }

    @Test
    void getByStatus_shouldReturn200() throws Exception {
        var date1 = LocalDate.of(2025, 12, 1);
        var date2 = LocalDate.of(2025, 12, 2);
        var departure1 = OffsetDateTime.now().plusDays(1);
        var departure2 = OffsetDateTime.now().plusDays(2);
        var trips = List.of(
                new TripResponse(1L, 10L, 50L, date1, departure1, departure1.plusHours(2), TripStatus.DEPARTED),
                new TripResponse(2L, 11L, 51L, date2, departure2, departure2.plusHours(3), TripStatus.DEPARTED)
        );
        var page = new PageImpl<>(trips, PageRequest.of(0, 10), 2);

        when(service.getByStatus(eq(TripStatus.DEPARTED), any())).thenReturn(page);

        mvc.perform(get("/api/v1/trips/by-status")
                        .param("status", "DEPARTED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].status").value("DEPARTED"))
                .andExpect(jsonPath("$.content[1].status").value("DEPARTED"));

        verify(service).getByStatus(eq(TripStatus.DEPARTED), any());
    }

    @Test
    void getByRouteIdAndStatus_shouldReturn200() throws Exception {
        var date1 = LocalDate.of(2025, 12, 1);
        var date2 = LocalDate.of(2025, 12, 2);
        var departure1 = OffsetDateTime.now().plusDays(1);
        var departure2 = OffsetDateTime.now().plusDays(2);
        var trips = List.of(
                new TripResponse(1L, 10L, 50L, date1, departure1, departure1.plusHours(2), TripStatus.SCHEDULED),
                new TripResponse(2L, 10L, 51L, date2, departure2, departure2.plusHours(2), TripStatus.SCHEDULED)
        );

        when(service.getByRouteIdAndStatus(10L, TripStatus.SCHEDULED)).thenReturn(trips);

        mvc.perform(get("/api/v1/routes/10/trips/search/by-status")
                        .param("status", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].route_id").value(10))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[1].route_id").value(10))
                .andExpect(jsonPath("$[1].status").value("SCHEDULED"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getByRouteIdAndStatus(10L, TripStatus.SCHEDULED);
    }

    @Test
    void getByDate_shouldReturn200() throws Exception {
        var date = LocalDate.of(2025, 12, 1);
        var departure1 = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0);
        var departure2 = OffsetDateTime.now().plusDays(1).withHour(14).withMinute(0);
        var trips = List.of(
                new TripResponse(1L, 10L, 50L, date, departure1, departure1.plusHours(2), TripStatus.SCHEDULED),
                new TripResponse(2L, 11L, 51L, date, departure2, departure2.plusHours(3), TripStatus.SCHEDULED)
        );
        var page = new PageImpl<>(trips, PageRequest.of(0, 10), 2);

        when(service.getByDate(eq(date), any())).thenReturn(page);

        mvc.perform(get("/api/v1/trips/by-date")
                        .param("date", date.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].date").value(date.toString()))
                .andExpect(jsonPath("$.content[1].date").value(date.toString()));

        verify(service).getByDate(eq(date), any());
    }

    @Test
    void getByRouteIdAndDate_shouldReturn200() throws Exception {
        var date = LocalDate.of(2025, 12, 1);
        var departure1 = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0);
        var departure2 = OffsetDateTime.now().plusDays(1).withHour(14).withMinute(0);
        var trips = List.of(
                new TripResponse(1L, 10L, 50L, date, departure1, departure1.plusHours(2), TripStatus.SCHEDULED),
                new TripResponse(2L, 10L, 51L, date, departure2, departure2.plusHours(2), TripStatus.SCHEDULED)
        );

        when(service.getByRouteIdAndDate(10L, date)).thenReturn(trips);

        mvc.perform(get("/api/v1/routes/10/trips/search")
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].route_id").value(10))
                .andExpect(jsonPath("$[0].date").value(date.toString()))
                .andExpect(jsonPath("$[1].route_id").value(10))
                .andExpect(jsonPath("$[1].date").value(date.toString()))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getByRouteIdAndDate(10L, date);
    }
}