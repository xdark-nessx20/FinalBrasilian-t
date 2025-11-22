package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.*;
import co.edu.unimagdalena.finalbrasiliant.services.RouteService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
class RouteControllerTest extends BaseTest {
    
    @MockitoBean
    RouteService service;

    @Test
    void createRoute_shouldReturn201AndLocation() throws Exception {
        var req = new RouteCreateRequest("RT001", "Santa Marta - Barranquilla", 
                "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120);
        var resp = new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120);

        when(service.create(any(RouteCreateRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/routes/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("RT001"))
                .andExpect(jsonPath("$.routeName").value("Santa Marta - Barranquilla"))
                .andExpect(jsonPath("$.origin").value("Santa Marta"))
                .andExpect(jsonPath("$.destination").value("Barranquilla"))
                .andExpect(jsonPath("$.distanceKM").value(95.50))
                .andExpect(jsonPath("$.durationMin").value(120));

        verify(service).create(any(RouteCreateRequest.class));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120);

        when(service.get(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("RT001"))
                .andExpect(jsonPath("$.routeName").value("Santa Marta - Barranquilla"));

        verify(service).get(1L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new RouteUpdateRequest("RT001-UPD", "Santa Marta - Barranquilla Express", 
                "Santa Marta", "Barranquilla", new BigDecimal("98.00"), 110);
        var resp = new RouteResponse(1L, "RT001-UPD", "Santa Marta - Barranquilla Express", 
                "Santa Marta", "Barranquilla", new BigDecimal("98.00"), 110);

        when(service.update(eq(1L), any(RouteUpdateRequest.class))).thenReturn(resp);

        mvc.perform(patch("/api/v1/routes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("RT001-UPD"))
                .andExpect(jsonPath("$.routeName").value("Santa Marta - Barranquilla Express"))
                .andExpect(jsonPath("$.distanceKM").value(98.00))
                .andExpect(jsonPath("$.durationMin").value(110));

        verify(service).update(eq(1L), any(RouteUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/routes/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void getByCode_shouldReturn200() throws Exception {
        var resp = new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120);

        when(service.getByCode("RT001")).thenReturn(resp);

        mvc.perform(get("/api/v1/routes/by-code")
                        .param("Code", "RT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("RT001"));

        verify(service).getByCode("RT001");
    }

    @Test
    void getByRouteName_shouldReturn200() throws Exception {
        var resp = new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120);

        when(service.getByRouteName("Santa Marta - Barranquilla")).thenReturn(resp);

        mvc.perform(get("/api/v1/routes/by-route-name")
                        .param("name", "Santa Marta - Barranquilla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.routeName").value("Santa Marta - Barranquilla"));

        verify(service).getByRouteName("Santa Marta - Barranquilla");
    }

    @Test
    void getByOrigin_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                        "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120),
                new RouteResponse(2L, "RT002", "Santa Marta - Cartagena", 
                        "Santa Marta", "Cartagena", new BigDecimal("220.00"), 240)
        );

        when(service.getByOrigin("Santa Marta")).thenReturn(routes);

        mvc.perform(get("/api/v1/routes/by-origin")
                        .param("origin", "Santa Marta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origin").value("Santa Marta"))
                .andExpect(jsonPath("$[1].origin").value("Santa Marta"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getByOrigin("Santa Marta");
    }

    @Test
    void getByDestination_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                        "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120),
                new RouteResponse(3L, "RT003", "Cartagena - Barranquilla", 
                        "Cartagena", "Barranquilla", new BigDecimal("120.00"), 150)
        );

        when(service.getByDestination("Barranquilla")).thenReturn(routes);

        mvc.perform(get("/api/v1/routes/by-destination")
                        .param("destination", "Barranquilla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].destination").value("Barranquilla"))
                .andExpect(jsonPath("$[1].destination").value("Barranquilla"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getByDestination("Barranquilla");
    }

    @Test
    void getByDurationGreaterThan_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(2L, "RT002", "Santa Marta - Cartagena", 
                        "Santa Marta", "Cartagena", new BigDecimal("220.00"), 240),
                new RouteResponse(4L, "RT004", "Bogotá - Medellín", 
                        "Bogotá", "Medellín", new BigDecimal("415.00"), 480)
        );

        when(service.getByMinDurationGreaterThan(200)).thenReturn(routes);

        mvc.perform(get("/api/v1/routes/by-duration-greater")
                        .param("minDuration", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].durationMin").value(240))
                .andExpect(jsonPath("$[1].durationMin").value(480))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getByMinDurationGreaterThan(200);
    }

    @Test
    void getByDurationBetween_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                        "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120),
                new RouteResponse(3L, "RT003", "Cartagena - Barranquilla", 
                        "Cartagena", "Barranquilla", new BigDecimal("120.00"), 150)
        );

        when(service.getByDurationBetween(100, 180)).thenReturn(routes);

        mvc.perform(get("/api/v1/routes/by-duration-between")
                        .param("min", "100")
                        .param("max", "180"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].durationMin").value(120))
                .andExpect(jsonPath("$[1].durationMin").value(150))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getByDurationBetween(100, 180);
    }

    @Test
    void getByDistanceLessThan_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                        "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120)
        );

        when(service.getByDistanceLessThan(new BigDecimal("100.00"))).thenReturn(routes);

        mvc.perform(get("/api/v1/routes/by-distance-lesser")
                        .param("maxDistance", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distanceKM").value(95.50))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).getByDistanceLessThan(new BigDecimal("100.00"));
    }

    @Test
    void getByDistanceBetween_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                        "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120),
                new RouteResponse(3L, "RT003", "Cartagena - Barranquilla", 
                        "Cartagena", "Barranquilla", new BigDecimal("120.00"), 150)
        );

        when(service.getByDistanceBetween(new BigDecimal("90.00"), new BigDecimal("150.00")))
                .thenReturn(routes);

        mvc.perform(get("/api/v1/routes/by-distance-between")
                        .param("min", "90.00")
                        .param("max", "150.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distanceKM").value(95.50))
                .andExpect(jsonPath("$[1].distanceKM").value(120.00))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getByDistanceBetween(new BigDecimal("90.00"), new BigDecimal("150.00"));
    }

    @Test
    void getByOriginAndDestination_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                        "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120),
                new RouteResponse(5L, "RT005", "Santa Marta - Barranquilla Express", 
                        "Santa Marta", "Barranquilla", new BigDecimal("98.00"), 110)
        );
        var page = new PageImpl<>(routes, PageRequest.of(0, 10), 2);

        when(service.getByOriginAndDestination(eq("Santa Marta"), eq("Barranquilla"), any()))
                .thenReturn(page);

        mvc.perform(get("/api/v1/routes/by-origin-and-destination")
                        .param("origin", "Santa Marta")
                        .param("destination", "Barranquilla")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].origin").value("Santa Marta"))
                .andExpect(jsonPath("$.content[0].destination").value("Barranquilla"))
                .andExpect(jsonPath("$.content[1].origin").value("Santa Marta"))
                .andExpect(jsonPath("$.content[1].destination").value("Barranquilla"));

        verify(service).getByOriginAndDestination(eq("Santa Marta"), eq("Barranquilla"), any());
    }

    @Test
    void getAllRoutes_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(1L, "RT001", "Santa Marta - Barranquilla", 
                        "Santa Marta", "Barranquilla", new BigDecimal("95.50"), 120),
                new RouteResponse(2L, "RT002", "Santa Marta - Cartagena", 
                        "Santa Marta", "Cartagena", new BigDecimal("220.00"), 240),
                new RouteResponse(3L, "RT003", "Cartagena - Barranquilla", 
                        "Cartagena", "Barranquilla", new BigDecimal("120.00"), 150)
        );
        var page = new PageImpl<>(routes, PageRequest.of(0, 10), 3);

        when(service.getAllRoutes(any())).thenReturn(page);

        mvc.perform(get("/api/v1/routes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[2].id").value(3));

        verify(service).getAllRoutes(any());
    }
}