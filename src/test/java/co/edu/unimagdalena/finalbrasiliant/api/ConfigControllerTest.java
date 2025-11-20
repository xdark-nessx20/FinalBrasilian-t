package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.services.ConfigService;
import co.edu.unimagdalena.finalbrasiliant.api.dto.ConfigDTOs.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigController.class)
class ConfigControllerTest extends BaseTest{
    @MockitoBean
    ConfigService service;

    @Test
    void createConfig_shouldReturn201AndLocation() throws Exception {
        var req = new ConfigCreateRequest("weight.limit", new BigDecimal("23.0"));
        var resp = new ConfigResponse("weight.limit", new BigDecimal("23.0"));

        when(service.create(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/admin/configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/admin/configs/weight.limit")))
                .andExpect(jsonPath("$.key").value("weight.limit"))
                .andExpect(jsonPath("$.value").value(23.0));

        verify(service).create(any(ConfigCreateRequest.class));
    }

    @Test
    void getConfig_shouldReturn200() throws Exception {
        var resp = new ConfigResponse("baggage.weight.fee", new BigDecimal("5.50"));

        when(service.get("baggage.weight.fee")).thenReturn(resp);

        mvc.perform(get("/api/v1/admin/configs/baggage.weight.fee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("baggage.weight.fee"))
                .andExpect(jsonPath("$.value").value(5.50));

        verify(service).get("baggage.weight.fee");
    }

    @Test
    void updateConfig_shouldReturn200() throws Exception {
        var req = new ConfigUpdateRequest(new BigDecimal("25.0"));
        var resp = new ConfigResponse("weight.limit", new BigDecimal("25.0"));

        when(service.update(eq("weight.limit"), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/admin/configs/weight.limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("weight.limit"))
                .andExpect(jsonPath("$.value").value(25.0));

        verify(service).update(eq("weight.limit"), any(ConfigUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/admin/configs/old.config"))
                .andExpect(status().isNoContent());

        verify(service).delete("old.config");
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        var configs = List.of(
                new ConfigResponse("weight.limit", new BigDecimal("23.0")),
                new ConfigResponse("baggage.weight.fee", new BigDecimal("5.50")),
                new ConfigResponse("ticket.no-show.fee", new BigDecimal("0.15")),
                new ConfigResponse("initial.refund.percent", new BigDecimal("0.90"))
        );

        when(service.getAll()).thenReturn(configs);

        mvc.perform(get("/api/v1/admin/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("weight.limit"))
                .andExpect(jsonPath("$[1].key").value("baggage.weight.fee"))
                .andExpect(jsonPath("$[2].key").value("ticket.no-show.fee"))
                .andExpect(jsonPath("$[3].key").value("initial.refund.percent"))
                .andExpect(jsonPath("$.length()").value(4));

        verify(service).getAll();
    }
}