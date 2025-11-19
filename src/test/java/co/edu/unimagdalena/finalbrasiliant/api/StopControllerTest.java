package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.StopDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.services.StopService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StopController.class)
public class StopControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    StopService service;

    private StopResponse stop1;
    private StopResponse stop2;
    private StopResponse stop3;
    private StopResponse stop4;
    private StopResponse stop5;

    @BeforeEach
    void setUp() {
        stop1 = new StopResponse(10L, 1L, "Terminal Barranquilla", 1, 10.9639, -74.7964);
        stop2 = new StopResponse(11L, 1L, "Ciénaga", 2, 11.0064, -74.2470);
        stop3 = new StopResponse(13L, 1L, "Fundación", 3, 10.5208, -74.1842);
        stop4 = new StopResponse(14L, 1L, "Aracataca", 4, 10.5883, -74.1842);
        stop5 = new StopResponse(12L, 1L, "Terminal Santa Marta", 5, 11.2408, -74.2010);
    }

    @Test
    void createStop_shouldReturn201AndLocation() throws Exception {
        var req = new StopCreateRequest(1L, "Terminal Barranquilla", 1, 10.9639, -74.7964);

        when(service.create(any())).thenReturn(stop1);

        mvc.perform(post("/api/v1/routes/1/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/stops/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.routeId").value(1))
                .andExpect(jsonPath("$.name").value("Terminal Barranquilla"))
                .andExpect(jsonPath("$.stopOrder").value(1))
                .andExpect(jsonPath("$.lat").value(10.9639))
                .andExpect(jsonPath("$.lng").value(-74.7964));

        verify(service).create(any(StopCreateRequest.class));
    }

    @Test
    void createStop_withMissingRequiredFields_shouldReturn400() throws Exception {
        var req = new StopCreateRequest(null, null, null, 10.9639, -74.7964);

        mvc.perform(post("/api/v1/routes/1/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void createStop_withBlankName_shouldReturn400() throws Exception {
        var req = new StopCreateRequest(1L, "", 1, 10.9639, -74.7964);

        mvc.perform(post("/api/v1/routes/1/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void createStop_withoutCoordinates_shouldReturn201() throws Exception {
        var req = new StopCreateRequest(1L, "Terminal Norte", 2, null, null);
        var resp = new StopResponse(11L, 1L, "Terminal Norte", 2, null, null);

        when(service.create(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/routes/1/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.lat").isEmpty())
                .andExpect(jsonPath("$.lng").isEmpty());

        verify(service).create(any(StopCreateRequest.class));
    }

    @Test
    void listByRoute_shouldReturn200() throws Exception {
        when(service.listByRoute(1L)).thenReturn(List.of(stop1, stop2, stop5));

        mvc.perform(get("/api/v1/routes/1/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeId").value(1))
                .andExpect(jsonPath("$[0].stopOrder").value(1))
                .andExpect(jsonPath("$[1].stopOrder").value(2))
                .andExpect(jsonPath("$[2].stopOrder").value(5))
                .andExpect(jsonPath("$.length()").value(3));

        verify(service).listByRoute(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        when(service.get(10L)).thenReturn(stop1);

        mvc.perform(get("/api/v1/stops/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Terminal Barranquilla"))
                .andExpect(jsonPath("$.stopOrder").value(1));

        verify(service).get(10L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new StopUpdateRequest("Terminal Central Barranquilla", 1, 10.9650, -74.7950);
        var updated = new StopResponse(10L, 1L, "Terminal Central Barranquilla", 1, 10.9650, -74.7950);

        when(service.update(eq(10L), any())).thenReturn(updated);

        mvc.perform(patch("/api/v1/stops/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Terminal Central Barranquilla"))
                .andExpect(jsonPath("$.lat").value(10.9650))
                .andExpect(jsonPath("$.lng").value(-74.7950));

        verify(service).update(eq(10L), any(StopUpdateRequest.class));
    }

    @Test
    void update_changingOnlyOrder_shouldReturn200() throws Exception {
        var req = new StopUpdateRequest(null, 5, null, null);
        var updated = new StopResponse(10L, 1L, "Terminal Barranquilla", 5, 10.9639, -74.7964);

        when(service.update(eq(10L), any())).thenReturn(updated);

        mvc.perform(patch("/api/v1/stops/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stopOrder").value(5));

        verify(service).update(eq(10L), any(StopUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/stops/10"))
                .andExpect(status().isNoContent());

        verify(service).delete(10L);
    }

    @Test
    void listByName_shouldReturn200() throws Exception {
        var stop25 = new StopResponse(25L, 3L, "Terminal de Barranquilla Norte", 1, 10.9700, -74.8000);

        when(service.listByName("Barranquilla")).thenReturn(List.of(stop1, stop25));

        mvc.perform(get("/api/v1/stops/by-name")
                        .param("name", "Barranquilla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Terminal Barranquilla"))
                .andExpect(jsonPath("$[1].name").value("Terminal de Barranquilla Norte"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByName("Barranquilla");
    }

    @Test
    void listByName_withNoMatches_shouldReturn200WithEmptyList() throws Exception {
        when(service.listByName("Inexistente")).thenReturn(List.of());

        mvc.perform(get("/api/v1/stops/by-name")
                        .param("name", "Inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service).listByName("Inexistente");
    }

    @Test
    void getFirstStop_shouldReturn200() throws Exception {
        when(service.getFirstStop(1L)).thenReturn(stop1);

        mvc.perform(get("/api/v1/routes/1/stops/first"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.stopOrder").value(1))
                .andExpect(jsonPath("$.name").value("Terminal Barranquilla"));

        verify(service).getFirstStop(1L);
    }

    @Test
    void getLastStop_shouldReturn200() throws Exception {
        when(service.getLastStop(1L)).thenReturn(stop5);

        mvc.perform(get("/api/v1/routes/1/stops/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.stopOrder").value(5))
                .andExpect(jsonPath("$.name").value("Terminal Santa Marta"));

        verify(service).getLastStop(1L);
    }

    @Test
    void getByRouteAndOrder_shouldReturn200() throws Exception {
        when(service.getByRouteAndOrder(1L, 2)).thenReturn(stop2);

        mvc.perform(get("/api/v1/routes/1/stops/by-order")
                        .param("stopOrder", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.routeId").value(1))
                .andExpect(jsonPath("$.stopOrder").value(2))
                .andExpect(jsonPath("$.name").value("Ciénaga"));

        verify(service).getByRouteAndOrder(1L, 2);
    }

    @Test
    void listByRouteAndOrderRange_shouldReturn200() throws Exception {
        when(service.listByRouteAndOrderRange(1L, 2, 4)).thenReturn(List.of(stop2, stop3, stop4));

        mvc.perform(get("/api/v1/routes/1/stops/by-order-range")
                        .param("startOrder", "2")
                        .param("endOrder", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stopOrder").value(2))
                .andExpect(jsonPath("$[1].stopOrder").value(3))
                .andExpect(jsonPath("$[2].stopOrder").value(4))
                .andExpect(jsonPath("$.length()").value(3));

        verify(service).listByRouteAndOrderRange(1L, 2, 4);
    }

    @Test
    void listByRouteAndOrderRange_withSingleStop_shouldReturn200() throws Exception {
        when(service.listByRouteAndOrderRange(1L, 2, 2)).thenReturn(List.of(stop2));

        mvc.perform(get("/api/v1/routes/1/stops/by-order-range")
                        .param("startOrder", "2")
                        .param("endOrder", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stopOrder").value(2))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).listByRouteAndOrderRange(1L, 2, 2);
    }

    @Test
    void listByRoute_withEmptyRoute_shouldReturn200WithEmptyList() throws Exception {
        when(service.listByRoute(99L)).thenReturn(List.of());

        mvc.perform(get("/api/v1/routes/99/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service).listByRoute(99L);
    }

    @Test
    void createStop_forDifferentRoutes_shouldReturn201() throws Exception {
        var req = new StopCreateRequest(2L, "Terminal Cartagena", 1, 10.3932, -75.4832);
        var resp = new StopResponse(20L, 2L, "Terminal Cartagena", 1, 10.3932, -75.4832);

        when(service.create(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/routes/2/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routeId").value(2))
                .andExpect(jsonPath("$.name").value("Terminal Cartagena"));

        verify(service).create(any(StopCreateRequest.class));
    }

    @Test
    void listByRouteAndOrderRange_withWideRange_shouldReturn200() throws Exception {
        when(service.listByRouteAndOrderRange(1L, 1, 5)).thenReturn(List.of(stop1, stop2, stop3, stop4, stop5));

        mvc.perform(get("/api/v1/routes/1/stops/by-order-range")
                        .param("startOrder", "1")
                        .param("endOrder", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        verify(service).listByRouteAndOrderRange(1L, 1, 5);
    }
}
