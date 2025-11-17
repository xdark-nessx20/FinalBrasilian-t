package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.services.AssignmentService;
import co.edu.unimagdalena.finalbrasiliant.api.dto.AssignmentDTOs.*;
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

@WebMvcTest(AssignmentController.class)
class AssignmentControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    AssignmentService service;

    @Test
    void createAssignment_shouldReturn201AndLocation() throws Exception {
        var req = new AssignmentCreateRequest(10L, 20L, true);
        var driver = new UserSummary(10L, "John Driver");
        var dispatcher = new UserSummary(20L, "Jane Dispatcher");
        var resp = new AssignmentResponse(5L, 1L, driver, dispatcher, true, OffsetDateTime.now());

        when(service.create(eq(1L), any())).thenReturn(resp);

        mvc.perform(post("/api/v1/trips/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/assignments/5")))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.tripId").value(1))
                .andExpect(jsonPath("$.driver.id").value(10))
                .andExpect(jsonPath("$.dispatcher.id").value(20))
                .andExpect(jsonPath("$.checkListOk").value(true));

        verify(service).create(eq(1L), any(AssignmentCreateRequest.class));
    }

    @Test
    void getByTrip_shouldReturn200() throws Exception {
        var driver = new UserSummary(10L, "John Driver");
        var dispatcher = new UserSummary(20L, "Jane Dispatcher");
        var resp = new AssignmentResponse(5L, 1L, driver, dispatcher, true, OffsetDateTime.now());

        when(service.getByTrip(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/trips/1/assignment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.tripId").value(1));

        verify(service).getByTrip(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var driver = new UserSummary(10L, "John Driver");
        var dispatcher = new UserSummary(20L, "Jane Dispatcher");
        var resp = new AssignmentResponse(5L, 1L, driver, dispatcher, false, OffsetDateTime.now());

        when(service.get(5L)).thenReturn(resp);

        mvc.perform(get("/api/v1/assignments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.checkListOk").value(false));

        verify(service).get(5L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new AssignmentUpdateRequest(true, 11L, 21L);
        var driver = new UserSummary(11L, "Updated Driver");
        var dispatcher = new UserSummary(21L, "Updated Dispatcher");
        var resp = new AssignmentResponse(5L, 1L, driver, dispatcher, true, OffsetDateTime.now());

        when(service.update(eq(5L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/assignments/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.checkListOk").value(true));

        verify(service).update(eq(5L), any(AssignmentUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/assignments/5"))
                .andExpect(status().isNoContent());

        verify(service).delete(5L);
    }

    @Test
    void listByDate_shouldReturn200() throws Exception {
        var start = OffsetDateTime.now().minusDays(7);
        var end = OffsetDateTime.now();
        var driver = new UserSummary(10L, "John Driver");
        var dispatcher = new UserSummary(20L, "Jane Dispatcher");
        var assignments = List.of(
                new AssignmentResponse(1L, 1L, driver, dispatcher, true, start.plusDays(1)),
                new AssignmentResponse(2L, 2L, driver, dispatcher, false, start.plusDays(2))
        );
        var page = new PageImpl<>(assignments, PageRequest.of(0, 10), 2);

        when(service.listByAssignedAt(any(OffsetDateTime.class), any(OffsetDateTime.class), any())).thenReturn(page);

        mvc.perform(get("/api/v1/assignments/by-date")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(service).listByAssignedAt(any(OffsetDateTime.class), any(OffsetDateTime.class), any());
    }

    @Test
    void listByDriver_shouldReturn200() throws Exception {
        var driver = new UserSummary(10L, "John Driver");
        var dispatcher = new UserSummary(20L, "Jane Dispatcher");
        var assignments = List.of(
                new AssignmentResponse(1L, 1L, driver, dispatcher, true, OffsetDateTime.now()),
                new AssignmentResponse(2L, 2L, driver, dispatcher, false, OffsetDateTime.now())
        );

        when(service.listByDriver(10L)).thenReturn(assignments);

        mvc.perform(get("/api/v1/assignments/by-driver")
                        .param("driverId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].driver.id").value(10));

        verify(service).listByDriver(10L);
    }

    @Test
    void listByCheckList_shouldReturn200() throws Exception {
        var driver = new UserSummary(10L, "John Driver");
        var dispatcher = new UserSummary(20L, "Jane Dispatcher");
        var assignments = List.of(
                new AssignmentResponse(1L, 1L, driver, dispatcher, true, OffsetDateTime.now()),
                new AssignmentResponse(3L, 3L, driver, dispatcher, true, OffsetDateTime.now())
        );

        when(service.listByCheckList(true)).thenReturn(assignments);

        mvc.perform(get("/api/v1/assignments/by-checklist")
                        .param("checkList", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].checkListOk").value(true))
                .andExpect(jsonPath("$[1].checkListOk").value(true))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByCheckList(true);
    }
}