package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BusDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import co.edu.unimagdalena.finalbrasiliant.services.BusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusController.class)
public class BusControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    BusService service;

    private BusResponse busResponse1;
    private BusResponse busResponse2;
    private BusResponse busResponse3;

    @BeforeEach
    void setUp() {

        busResponse1 = new BusResponse(1L, "ABC-123", 40, Set.of("WiFi", "AC"), BusStatus.AVAILABLE);
        busResponse2 = new BusResponse(2L, "XYZ-789", 45, Set.of("AC"), BusStatus.AVAILABLE);
        busResponse3 = new BusResponse(3L, "DEF-456", 50, Set.of("WiFi", "AC"), BusStatus.ON_ROUTE);
    }

    @Test
    void createBus_shouldReturn201AndLocation() throws Exception {
        var req = new BusCreateRequest("ABC-123", 40, Set.of("WiFi", "AC"), BusStatus.AVAILABLE);

        when(service.create(any())).thenReturn(busResponse1);

        mvc.perform(post("/api/v1/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/buses/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("ABC-123"))
                .andExpect(jsonPath("$.capacity").value(40))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.amenities").isArray())
                .andExpect(jsonPath("$.amenities.length()").value(2));

        verify(service).create(any(BusCreateRequest.class));
    }

    @Test
    void createBus_withInvalidPlate_shouldReturn400() throws Exception {
        var req = new BusCreateRequest("AB", 40, Set.of("WiFi"), BusStatus.AVAILABLE);

        mvc.perform(post("/api/v1/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void createBus_withInvalidCapacity_shouldReturn400() throws Exception {
        var req = new BusCreateRequest("ABC-123", 5, Set.of("WiFi"), BusStatus.AVAILABLE);

        mvc.perform(post("/api/v1/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void get_shouldReturn200() throws Exception {
        when(service.get(1L)).thenReturn(busResponse1);

        mvc.perform(get("/api/v1/buses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("ABC-123"))
                .andExpect(jsonPath("$.capacity").value(40))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        verify(service).get(1L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new BusUpdateRequest("XYZ-789", 45, Set.of("WiFi", "AC", "USB"), BusStatus.ON_ROUTE);
        var updated = new BusResponse(1L, "XYZ-789", 45, Set.of("WiFi", "AC", "USB"), BusStatus.ON_ROUTE);

        when(service.update(eq(1L), any())).thenReturn(updated);

        mvc.perform(patch("/api/v1/buses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("XYZ-789"))
                .andExpect(jsonPath("$.capacity").value(45))
                .andExpect(jsonPath("$.status").value("ON_ROUTE"))
                .andExpect(jsonPath("$.amenities.length()").value(3));

        verify(service).update(eq(1L), any(BusUpdateRequest.class));
    }

    @Test
    void update_withInvalidPlate_shouldReturn400() throws Exception {
        var req = new BusUpdateRequest("AB", 45, Set.of("WiFi"), BusStatus.MAINTENANCE);

        mvc.perform(patch("/api/v1/buses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).update(anyLong(), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/buses/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void getByPlate_shouldReturn200() throws Exception {
        when(service.getByPlate("ABC-123")).thenReturn(busResponse1);

        mvc.perform(get("/api/v1/buses/by-plate")
                        .param("plate", "ABC-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("ABC-123"));

        verify(service).getByPlate("ABC-123");
    }

    @Test
    void listByStatus_shouldReturn200() throws Exception {
        when(service.listByStatus(BusStatus.AVAILABLE)).thenReturn(List.of(busResponse1, busResponse2));

        mvc.perform(get("/api/v1/buses/by-status")
                        .param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByStatus(BusStatus.AVAILABLE);
    }

    @Test
    void listByCapacity_shouldReturn200() throws Exception {
        when(service.listByCapacityGreaterThanEqual(40)).thenReturn(List.of(busResponse1, busResponse2, busResponse3));

        mvc.perform(get("/api/v1/buses/by-capacity")
                        .param("capacity", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].capacity").value(40))
                .andExpect(jsonPath("$[1].capacity").value(45))
                .andExpect(jsonPath("$[2].capacity").value(50))
                .andExpect(jsonPath("$.length()").value(3));

        verify(service).listByCapacityGreaterThanEqual(40);
    }

    @Test
    void listByAmenities_shouldReturn200() throws Exception {
        var withWifiAC = new BusResponse(2L, "XYZ-789", 45, Set.of("WiFi", "AC", "USB"), BusStatus.MAINTENANCE);

        when(service.listByAmenities(Set.of("WiFi", "AC"))).thenReturn(List.of(busResponse1, withWifiAC));

        mvc.perform(get("/api/v1/buses/by-amenities")
                        .param("amenities", "WiFi", "AC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByAmenities(Set.of("WiFi", "AC"));
    }

    @Test
    void listByAmenities_withSingleAmenity_shouldReturn200() throws Exception {
        when(service.listByAmenities(Set.of("WiFi"))).thenReturn(List.of(busResponse1, busResponse3));

        mvc.perform(get("/api/v1/buses/by-amenities")
                        .param("amenities", "WiFi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByAmenities(Set.of("WiFi"));
    }

    @Test
    void listByAmenities_withEmptySet_shouldReturn200WithEmptyList() throws Exception {
        when(service.listByAmenities(Set.of())).thenReturn(List.of());

        mvc.perform(get("/api/v1/buses/by-amenities")
                        .param("amenities", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service).listByAmenities(any());
    }
}
