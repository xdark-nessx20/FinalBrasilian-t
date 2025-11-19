package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.EntityType;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.IncidentType;
import co.edu.unimagdalena.finalbrasiliant.services.IncidentService;
import co.edu.unimagdalena.finalbrasiliant.api.dto.IncidentDTOs.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    IncidentService service;

    @Test
    void createIncident_shouldReturn201AndLocation() throws Exception {
        var req = new IncidentCreateRequest(EntityType.TICKET, 100L, IncidentType.VEHICLE, "Bus delayed by 2 hours");
        var resp = new IncidentResponse(1L, EntityType.TICKET, 100L, IncidentType.VEHICLE, OffsetDateTime.now(), "Bus delayed by 2 hours");

        when(service.create(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/incidents/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.entityType").value("TICKET"))
                .andExpect(jsonPath("$.entityId").value(100))
                .andExpect(jsonPath("$.type").value("VEHICLE"))
                .andExpect(jsonPath("$.note").value("Bus delayed by 2 hours"));

        verify(service).create(any(IncidentCreateRequest.class));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new IncidentResponse(1L, EntityType.PARCEL, 200L, IncidentType.DELIVERY_FAIL, OffsetDateTime.now(), "Package damaged during transport");

        when(service.get(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/incidents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.entityType").value("PARCEL"))
                .andExpect(jsonPath("$.entityId").value(200))
                .andExpect(jsonPath("$.type").value("DELIVERY_FAIL"));

        verify(service).get(1L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new IncidentUpdateRequest("Updated note: Issue resolved", 100L);
        var resp = new IncidentResponse(1L, EntityType.TICKET, 100L, IncidentType.VEHICLE, OffsetDateTime.now(), "Updated note: Issue resolved");

        when(service.update(eq(1L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/incidents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.note").value("Updated note: Issue resolved"));

        verify(service).update(eq(1L), any(IncidentUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/incidents/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void list_shouldReturn200() throws Exception {
        var incidents = List.of(
                new IncidentResponse(1L, EntityType.TICKET, 100L, IncidentType.VEHICLE, OffsetDateTime.now(), "Note 1"),
                new IncidentResponse(2L, EntityType.PARCEL, 200L, IncidentType.SECURITY, OffsetDateTime.now(), "Note 2")
        );
        var page = new PageImpl<>(incidents, PageRequest.of(0, 10), 2);

        when(service.getAll(any())).thenReturn(page);

        mvc.perform(get("/api/v1/incidents")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(service).getAll(any());
    }

    @Test
    void listByDate_shouldReturn200() throws Exception {
        var from = OffsetDateTime.now().minusDays(7);
        var to = OffsetDateTime.now();
        var incidents = List.of(
                new IncidentResponse(1L, EntityType.TRIP, 300L, IncidentType.SECURITY, from.plusDays(1), "Trip cancelled"),
                new IncidentResponse(2L, EntityType.TICKET, 100L, IncidentType.VEHICLE, from.plusDays(2), "Delayed")
        );
        var page = new PageImpl<>(incidents, PageRequest.of(0, 10), 2);

        when(service.listByCreatedAt(any(OffsetDateTime.class), any(OffsetDateTime.class), any())).thenReturn(page);

        mvc.perform(get("/api/v1/incidents/by-date")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(service).listByCreatedAt(any(OffsetDateTime.class), any(OffsetDateTime.class), any());
    }

    @Test
    void listByTypeAndEType_withBothParams_shouldReturn200() throws Exception {
        var incidents = List.of(
                new IncidentResponse(1L, EntityType.TICKET, 100L, IncidentType.VEHICLE, OffsetDateTime.now(), "Note 1"),
                new IncidentResponse(2L, EntityType.TICKET, 101L, IncidentType.VEHICLE, OffsetDateTime.now(), "Note 2")
        );

        when(service.listByTypeAndEntityType(IncidentType.VEHICLE, EntityType.TICKET)).thenReturn(incidents);

        mvc.perform(get("/api/v1/incidents/by-typeAndEntity")
                        .param("type", "DELAY")
                        .param("entity", "TICKET"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("VEHICLE"))
                .andExpect(jsonPath("$[0].entityType").value("TICKET"))
                .andExpect(jsonPath("$[1].type").value("VEHICLE"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByTypeAndEntityType(IncidentType.VEHICLE, EntityType.TICKET);
    }

    @Test
    void listByTypeAndEType_withOnlyType_shouldReturn200() throws Exception {
        var incidents = List.of(
                new IncidentResponse(1L, EntityType.PARCEL, 100L, IncidentType.DELIVERY_FAIL, OffsetDateTime.now(), "Note 1"),
                new IncidentResponse(2L, EntityType.PARCEL, 200L, IncidentType.DELIVERY_FAIL, OffsetDateTime.now(), "Note 2")
        );

        when(service.listByTypeAndEntityType(IncidentType.DELIVERY_FAIL, null)).thenReturn(incidents);

        mvc.perform(get("/api/v1/incidents/by-typeAndEntity")
                        .param("type", "DAMAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DELIVERY_FAIL"))
                .andExpect(jsonPath("$[1].type").value("DELIVERY_FAIL"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByTypeAndEntityType(IncidentType.DELIVERY_FAIL, null);
    }

    @Test
    void listByTypeAndEType_withOnlyEntity_shouldReturn200() throws Exception {
        var incidents = List.of(
                new IncidentResponse(1L, EntityType.PARCEL, 200L, IncidentType.DELIVERY_FAIL, OffsetDateTime.now(), "Note 1"),
                new IncidentResponse(2L, EntityType.PARCEL, 201L, IncidentType.SECURITY, OffsetDateTime.now(), "Note 2")
        );

        when(service.listByTypeAndEntityType(null, EntityType.PARCEL)).thenReturn(incidents);

        mvc.perform(get("/api/v1/incidents/by-typeAndEntity")
                        .param("entity", "PARCEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityType").value("PARCEL"))
                .andExpect(jsonPath("$[1].entityType").value("PARCEL"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByTypeAndEntityType(null, EntityType.PARCEL);
    }
}