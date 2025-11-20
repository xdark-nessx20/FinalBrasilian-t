package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.ParcelStatus;
import co.edu.unimagdalena.finalbrasiliant.services.ParcelService;
import co.edu.unimagdalena.finalbrasiliant.api.dto.ParcelDTOs.*;
import org.jetbrains.annotations.NotNull;
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

@WebMvcTest(ParcelController.class)
class ParcelControllerTest extends BaseTest{
    @MockitoBean
    ParcelService service;

    @Test
    void createParcel_shouldReturn201AndLocation() throws Exception {
        var req = new ParcelCreateRequest(1L, 2L, new BigDecimal("50000"), "Juan Sender",
                "+573001234567", "Maria Receiver", "+573007654321");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(2L, "Terminal Sur", 5);
        var resp = new ParcelResponse(10L, "PAR-ABC123DEF456", fromStop, toStop,
                new BigDecimal("50000"), ParcelStatus.CREATED, "Juan Sender", "+573001234567",
                "Maria Receiver", "+573007654321", null);

        when(service.create(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/parcels/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.code").value("PAR-ABC123DEF456"))
                .andExpect(jsonPath("$.fromStop.id").value(1))
                .andExpect(jsonPath("$.toStop.id").value(2))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.senderName").value("Juan Sender"))
                .andExpect(jsonPath("$.receiverName").value("Maria Receiver"));

        verify(service).create(any(ParcelCreateRequest.class));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(2L, "Terminal Sur", 5);
        var resp = new ParcelResponse(10L, "PAR-ABC123DEF456", fromStop, toStop,
                new BigDecimal("50000"), ParcelStatus.IN_TRANSIT, "Juan Sender", "+573001234567",
                "Maria Receiver", "+573007654321", null);

        when(service.get(10L)).thenReturn(resp);

        mvc.perform(get("/api/v1/parcels/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));

        verify(service).get(10L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new ParcelUpdateRequest(new BigDecimal("55000"), ParcelStatus.DELIVERED,
                "+573001234567", "Maria Receiver Updated", "+573007654321");
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(2L, "Terminal Sur", 5);
        var resp = new ParcelResponse(10L, "PAR-ABC123DEF456", fromStop, toStop,
                new BigDecimal("55000"), ParcelStatus.DELIVERED, "Juan Sender", "+573001234567",
                "Maria Receiver Updated", "+573007654321", null);

        when(service.update(eq(10L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/parcels/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.price").value(55000))
                .andExpect(jsonPath("$.receiverName").value("Maria Receiver Updated"));

        verify(service).update(eq(10L), any(ParcelUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/parcels/10"))
                .andExpect(status().isNoContent());

        verify(service).delete(10L);
    }

    @Test
    void getByCode_shouldReturn200() throws Exception {
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(2L, "Terminal Sur", 5);
        var resp = new ParcelResponse(10L, "PAR-ABC123DEF456", fromStop, toStop,
                new BigDecimal("50000"), ParcelStatus.CREATED, "Juan Sender", "+573001234567",
                "Maria Receiver", "+573007654321", null);

        when(service.getByCode("PAR-ABC123DEF456")).thenReturn(resp);

        mvc.perform(get("/api/v1/parcels/by-code")
                        .param("code", "PAR-ABC123DEF456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PAR-ABC123DEF456"))
                .andExpect(jsonPath("$.id").value(10));

        verify(service).getByCode("PAR-ABC123DEF456");
    }

    @Test
    void listBySender_shouldReturn200() throws Exception {
        var parcels = getResponseList();

        when(service.listBySender("Juan Sender")).thenReturn(parcels);

        mvc.perform(get("/api/v1/parcels/by-sender")
                        .param("senderName", "Juan Sender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderName").value("Juan Sender"))
                .andExpect(jsonPath("$[1].senderName").value("Juan Sender"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listBySender("Juan Sender");
    }

    private static @NotNull List<ParcelResponse> getResponseList() {
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(2L, "Terminal Sur", 5);

        return List.of(
                new ParcelResponse(10L, "PAR-CODE001", fromStop, toStop, new BigDecimal("50000"),
                        ParcelStatus.CREATED, "Juan Sender", "+573001234567", "Maria", "+573007654321", null),
                new ParcelResponse(11L, "PAR-CODE002", fromStop, toStop, new BigDecimal("60000"),
                        ParcelStatus.IN_TRANSIT, "Juan Sender", "+573001234567", "Pedro", "+573001111111", null)
        );
    }

    @Test
    void listByReceiver_shouldReturn200() throws Exception {
        var parcels = getParcelResponseList();

        when(service.listBySender("Maria Receiver")).thenReturn(parcels);

        mvc.perform(get("/api/v1/parcels/by-receiver")
                        .param("receiverName", "Maria Receiver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiverName").value("Maria Receiver"))
                .andExpect(jsonPath("$[1].receiverName").value("Maria Receiver"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listBySender("Maria Receiver");
    }

    private static @NotNull List<ParcelResponse> getParcelResponseList() {
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(2L, "Terminal Sur", 5);

        return List.of(
                new ParcelResponse(10L, "PAR-CODE001", fromStop, toStop, new BigDecimal("50000"),
                        ParcelStatus.READY_FOR_PICKUP, "Juan", "+573001234567", "Maria Receiver", "+573007654321", "OTP123456"),
                new ParcelResponse(11L, "PAR-CODE002", fromStop, toStop, new BigDecimal("60000"),
                        ParcelStatus.DELIVERED, "Pedro", "+573001111111", "Maria Receiver", "+573007654321", null)
        );
    }

    @Test
    void getByOTP_shouldReturn200() throws Exception {
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(2L, "Terminal Sur", 5);
        var resp = new ParcelResponse(10L, "PAR-ABC123DEF456", fromStop, toStop,
                new BigDecimal("50000"), ParcelStatus.READY_FOR_PICKUP, "Juan Sender", "+573001234567",
                "Maria Receiver", "+573007654321", "OTP1234567");

        when(service.getByDeliveryOTP("OTP1234567")).thenReturn(resp);

        mvc.perform(get("/api/v1/parcels/by-OTP")
                        .param("otp", "OTP1234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryOTP").value("OTP1234567"))
                .andExpect(jsonPath("$.status").value("READY_FOR_PICKUP"));

        verify(service).getByDeliveryOTP("OTP1234567");
    }

    @Test
    void listByStretch_withBothStops_shouldReturn200() throws Exception {
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(2L, "Terminal Sur", 5);
        var parcels = List.of(
                new ParcelResponse(10L, "PAR-CODE001", fromStop, toStop, new BigDecimal("50000"),
                        ParcelStatus.IN_TRANSIT, "Juan", "+573001234567", "Maria", "+573007654321", null)
        );

        when(service.listByStretch(1L, 2L)).thenReturn(parcels);

        mvc.perform(get("/api/v1/parcels/by-stretch")
                        .param("fromStop", "1")
                        .param("toStop", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStop.id").value(1))
                .andExpect(jsonPath("$[0].toStop.id").value(2))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).listByStretch(1L, 2L);
    }

    @Test
    void listByStretch_withOnlyFromStop_shouldReturn200() throws Exception {
        var parcels = getResponses();

        when(service.listByStretch(1L, null)).thenReturn(parcels);

        mvc.perform(get("/api/v1/parcels/by-stretch")
                        .param("fromStop", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStop.id").value(1))
                .andExpect(jsonPath("$[1].fromStop.id").value(1))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).listByStretch(1L, null);
    }

    private static @NotNull List<ParcelResponse> getResponses() {
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop1 = new StopSummary(2L, "Terminal Sur", 5);
        var toStop2 = new StopSummary(3L, "Terminal Este", 8);

        return List.of(
                new ParcelResponse(10L, "PAR-CODE001", fromStop, toStop1, new BigDecimal("50000"),
                        ParcelStatus.CREATED, "Juan", "+573001234567", "Maria", "+573007654321", null),
                new ParcelResponse(11L, "PAR-CODE002", fromStop, toStop2, new BigDecimal("60000"),
                        ParcelStatus.IN_TRANSIT, "Pedro", "+573001111111", "Ana", "+573002222222", null)
        );
    }

    @Test
    void listByStatus_shouldReturn200() throws Exception {
        var parcels = getParcelResponses();
        var page = new PageImpl<>(parcels, PageRequest.of(0, 10), 2);

        when(service.listByStatus(eq(ParcelStatus.DELIVERED), any())).thenReturn(page);

        mvc.perform(get("/api/v1/parcels/by-status")
                        .param("status", "DELIVERED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].status").value("DELIVERED"))
                .andExpect(jsonPath("$.content[1].status").value("DELIVERED"));

        verify(service).listByStatus(eq(ParcelStatus.DELIVERED), any());
    }

    private static @NotNull List<ParcelResponse> getParcelResponses() {
        var fromStop = new StopSummary(1L, "Terminal Norte", 1);
        var toStop = new StopSummary(2L, "Terminal Sur", 5);
        return List.of(
                new ParcelResponse(10L, "PAR-CODE001", fromStop, toStop, new BigDecimal("50000"),
                        ParcelStatus.DELIVERED, "Juan", "+573001234567", "Maria", "+573007654321", null),
                new ParcelResponse(11L, "PAR-CODE002", fromStop, toStop, new BigDecimal("60000"),
                        ParcelStatus.DELIVERED, "Pedro", "+573001111111", "Ana", "+573002222222", null)
        );
    }
}