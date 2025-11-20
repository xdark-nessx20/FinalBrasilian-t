package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.FareRuleDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;
import co.edu.unimagdalena.finalbrasiliant.services.FareRuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FareRuleController.class)
public class FareRuleControllerTest extends BaseTest{
    @MockitoBean
    FareRuleService service;

    private RouteSummary routeSummary;
    private StopSummary fromStop;
    private StopSummary toStop;
    private StopSummary midStop;
    private FareRuleResponse fareResponse1;
    private FareRuleResponse fareResponse2;
    private Map<String, BigDecimal> discounts;

    @BeforeEach
    void setUp() {
        routeSummary = new RouteSummary(1L, "Barranquilla - Santa Marta");
        fromStop = new StopSummary(10L, "Terminal Barranquilla", 1);
        toStop = new StopSummary(20L, "Terminal Santa Marta", 5);
        midStop = new StopSummary(15L, "Ciénaga", 3);
        discounts = new HashMap<>(Map.of("STUDENT", BigDecimal.valueOf(15), "SENIOR", BigDecimal.valueOf(20)));

        fareResponse1 = new FareRuleResponse(100L, routeSummary, fromStop, toStop,
                new BigDecimal("50000"), discounts, DynamicPricing.ON);
        fareResponse2 = new FareRuleResponse(101L, routeSummary, fromStop, midStop,
                new BigDecimal("30000"), new HashMap<>(Map.of("STUDENT", BigDecimal.valueOf(15))), DynamicPricing.ON);
    }

    @Test
    void createFareRule_shouldReturn201AndLocation() throws Exception {
        var req = new FareRuleCreateRequest(1L, 10L, 20L, new BigDecimal("50000"), discounts, DynamicPricing.ON);

        when(service.create(any())).thenReturn(fareResponse1);

        mvc.perform(post("/api/v1/fare-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/fare-rules/100")))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.route.id").value(1))
                .andExpect(jsonPath("$.route.routeName").value("Barranquilla - Santa Marta"))
                .andExpect(jsonPath("$.fromStop.id").value(10))
                .andExpect(jsonPath("$.fromStop.name").value("Terminal Barranquilla"))
                .andExpect(jsonPath("$.toStop.id").value(20))
                .andExpect(jsonPath("$.toStop.name").value("Terminal Santa Marta"))
                .andExpect(jsonPath("$.basePrice").value(50000))
                .andExpect(jsonPath("$.dynamicPricing").value("ON"))
                .andExpect(jsonPath("$.discounts.STUDENT").value(15))
                .andExpect(jsonPath("$.discounts.SENIOR").value(20));

        verify(service).create(any(FareRuleCreateRequest.class));
    }

    @Test
    void createFareRule_withMissingRequiredFields_shouldReturn400() throws Exception {
        var req = new FareRuleCreateRequest(null, null, null,
                new BigDecimal("50000"), new HashMap<>(Map.of()), DynamicPricing.ON);

        mvc.perform(post("/api/v1/fare-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void get_shouldReturn200() throws Exception {
        when(service.get(100L)).thenReturn(fareResponse1);

        mvc.perform(get("/api/v1/fare-rules/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.basePrice").value(50000))
                .andExpect(jsonPath("$.dynamicPricing").value("ON"));

        verify(service).get(100L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new FareRuleUpdateRequest(new BigDecimal("55000"),
                new HashMap<>(Map.of("STUDENT", BigDecimal.valueOf(20), "SENIOR", BigDecimal.valueOf(25))), DynamicPricing.OFF);
        var updated = new FareRuleResponse(100L, routeSummary, fromStop, toStop,
                new BigDecimal("55000"), Map.of("STUDENT", BigDecimal.valueOf(20), "SENIOR", BigDecimal.valueOf(25)), DynamicPricing.OFF);

        when(service.update(eq(100L), any())).thenReturn(updated);

        mvc.perform(patch("/api/v1/fare-rules/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.basePrice").value(55000))
                .andExpect(jsonPath("$.dynamicPricing").value("OFF"))
                .andExpect(jsonPath("$.discounts.STUDENT").value(20))
                .andExpect(jsonPath("$.discounts.SENIOR").value(25));

        verify(service).update(eq(100L), any(FareRuleUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/fare-rules/100"))
                .andExpect(status().isNoContent());

        verify(service).delete(100L);
    }

    @Test
    void getByRouteAndStops_shouldReturn200() throws Exception {
        when(service.getByRouteAndStops(1L, 10L, 20L)).thenReturn(fareResponse1);

        mvc.perform(get("/api/v1/fare-rules/by-route-and-stops")
                        .param("routeId", "1")
                        .param("fromStopId", "10")
                        .param("toStopId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.route.id").value(1))
                .andExpect(jsonPath("$.fromStop.id").value(10))
                .andExpect(jsonPath("$.toStop.id").value(20));

        verify(service).getByRouteAndStops(1L, 10L, 20L);
    }

    @Test
    void listByRoute_shouldReturn200() throws Exception {
        when(service.listByRoute(1L)).thenReturn(List.of(fareResponse1, fareResponse2));

        mvc.perform(get("/api/v1/fare-rules/by-route")
                        .param("routeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].route.id").value(1))
                .andExpect(jsonPath("$[1].id").value(101))
                .andExpect(jsonPath("$[1].route.id").value(1))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByRoute(1L);
    }

    @Test
    void listByFromStop_shouldReturn200() throws Exception {
        when(service.listByFromStop(10L)).thenReturn(List.of(fareResponse1, fareResponse2));

        mvc.perform(get("/api/v1/fare-rules/by-from-stop")
                        .param("fromStopId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStop.id").value(10))
                .andExpect(jsonPath("$[1].fromStop.id").value(10))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByFromStop(10L);
    }

    @Test
    void listByToStop_shouldReturn200() throws Exception {
        var fare3 = new FareRuleResponse(102L, routeSummary, midStop, toStop,
                new BigDecimal("20000"), new HashMap<>(Map.of("STUDENT", BigDecimal.valueOf(20))), DynamicPricing.ON);

        when(service.listByToStop(20L)).thenReturn(List.of(fareResponse1, fare3));

        mvc.perform(get("/api/v1/fare-rules/by-to-stop")
                        .param("toStopId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toStop.id").value(20))
                .andExpect(jsonPath("$[1].toStop.id").value(20))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByToStop(20L);
    }

    @Test
    void listByDynamicPricing_shouldReturn200() throws Exception {
        when(service.listByDynamicPricing(DynamicPricing.ON)).thenReturn(List.of(fareResponse1, fareResponse2));

        mvc.perform(get("/api/v1/fare-rules/by-dynamic-pricing")
                        .param("dynamicPricing", "ON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dynamicPricing").value("ON"))
                .andExpect(jsonPath("$[1].dynamicPricing").value("ON"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByDynamicPricing(DynamicPricing.ON);
    }

    @Test
    void listByRouteAndDynamicPricing_shouldReturn200() throws Exception {
        var fare3 = new FareRuleResponse(103L, routeSummary, fromStop, midStop,
                new BigDecimal("25000"), new HashMap<>(Map.of()), DynamicPricing.OFF);

        when(service.listByRouteAndDynamicPricing(1L, DynamicPricing.OFF)).thenReturn(List.of(fare3));

        mvc.perform(get("/api/v1/fare-rules/by-route-and-pricing")
                        .param("routeId", "1")
                        .param("dynamicPricing", "OFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].route.id").value(1))
                .andExpect(jsonPath("$[0].dynamicPricing").value("OFF"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).listByRouteAndDynamicPricing(1L, DynamicPricing.OFF);
    }

    @Test
    void listByDynamicPricing_withOFF_shouldReturn200() throws Exception {
        var routeSummary2 = new RouteSummary(2L, "Cartagena - Montería");
        var fromStop2 = new StopSummary(30L, "Terminal Cartagena", 1);
        var toStop2 = new StopSummary(40L, "Terminal Montería", 8);
        var fareOff = new FareRuleResponse(200L, routeSummary2, fromStop2, toStop2,
                new BigDecimal("60000"), new HashMap<>(Map.of("SENIOR", BigDecimal.valueOf(30))), DynamicPricing.OFF);

        when(service.listByDynamicPricing(DynamicPricing.OFF)).thenReturn(List.of(fareOff));

        mvc.perform(get("/api/v1/fare-rules/by-dynamic-pricing")
                        .param("dynamicPricing", "OFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(200))
                .andExpect(jsonPath("$[0].dynamicPricing").value("OFF"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).listByDynamicPricing(DynamicPricing.OFF);
    }

    @Test
    void createFareRule_withEmptyDiscounts_shouldReturn201() throws Exception {
        var req = new FareRuleCreateRequest(1L, 10L, 20L, new BigDecimal("50000"), Map.of(), DynamicPricing.ON);
        var resp = new FareRuleResponse(100L, routeSummary, fromStop, toStop,
                new BigDecimal("50000"), new HashMap<>(Map.of()), DynamicPricing.ON);

        when(service.create(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/fare-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.discounts").isEmpty());

        verify(service).create(any(FareRuleCreateRequest.class));
    }
}
