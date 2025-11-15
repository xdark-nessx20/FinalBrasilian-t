package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Seat;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.SeatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SeatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BusRepository busRepository;

    private Bus testBus;

    @BeforeEach
    void setUp() {
        seatRepository.deleteAll();
        busRepository.deleteAll();

        // Create test bus
        testBus = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(BusStatus.AVAILABLE)
                .build();
        testBus = busRepository.save(testBus);
    }

    @Test
    void testCreateSeat_Success() throws Exception {
        // Given
        SeatCreateRequest request = new SeatCreateRequest(
                testBus.getId(),
                "A1",
                SeatType.STANDARD
        );

        // When & Then
        mockMvc.perform(post("/api/v1/buses/{busId}/seats", testBus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.bus_id").value(testBus.getId()))
                .andExpect(jsonPath("$.number").value("A1"))
                .andExpect(jsonPath("$.type").value("STANDARD"));
    }

    @Test
    void testGetSeat_Success() throws Exception {
        // Given
        Seat seat = Seat.builder()
                .bus(testBus)
                .number("B2")
                .type(SeatType.PREFERENTIAL)
                .build();
        Seat savedSeat = seatRepository.save(seat);

        // When & Then
        mockMvc.perform(get("/api/v1/seats/{id}", savedSeat.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedSeat.getId()))
                .andExpect(jsonPath("$.bus_id").value(testBus.getId()))
                .andExpect(jsonPath("$.number").value("B2"))
                .andExpect(jsonPath("$.type").value("PREFERENTIAL"));
    }

    @Test
    void testUpdateSeat_Success() throws Exception {
        // Given
        Seat seat = Seat.builder()
                .bus(testBus)
                .number("C3")
                .type(SeatType.STANDARD)
                .build();
        Seat savedSeat = seatRepository.save(seat);

        SeatUpdateRequest updateRequest = new SeatUpdateRequest(
                testBus.getId(),
                "C4",
                SeatType.PREFERENTIAL
        );

        // When & Then
        mockMvc.perform(patch("/api/v1/seats/{id}", savedSeat.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedSeat.getId()))
                .andExpect(jsonPath("$.number").value("C4"))
                .andExpect(jsonPath("$.type").value("PREFERENTIAL"));
    }

    @Test
    void testDeleteSeat_Success() throws Exception {
        // Given
        Seat seat = Seat.builder()
                .bus(testBus)
                .number("D5")
                .type(SeatType.STANDARD)
                .build();
        Seat savedSeat = seatRepository.save(seat);

        // When & Then
        mockMvc.perform(delete("/api/v1/seats/{id}", savedSeat.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetSeatsByBus_Success() throws Exception {
        // Given
        Seat seat1 = Seat.builder()
                .bus(testBus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        Seat seat2 = Seat.builder()
                .bus(testBus)
                .number("A2")
                .type(SeatType.STANDARD)
                .build();

        Seat seat3 = Seat.builder()
                .bus(testBus)
                .number("B1")
                .type(SeatType.PREFERENTIAL)
                .build();

        seatRepository.save(seat1);
        seatRepository.save(seat2);
        seatRepository.save(seat3);

        // When & Then
        mockMvc.perform(get("/api/v1/buses/{busId}/seats", testBus.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].bus_id", everyItem(is(testBus.getId().intValue()))))
                .andExpect(jsonPath("$[*].number", containsInAnyOrder("A1", "A2", "B1")));
    }

    @Test
    void testGetSeatByNumberAndBus_Success() throws Exception {
        // Given
        Seat seat = Seat.builder()
                .bus(testBus)
                .number("E5")
                .type(SeatType.STANDARD)
                .build();
        seatRepository.save(seat);

        // When & Then
        mockMvc.perform(get("/api/v1/buses/{busId}/seats/by-number/{number}", 
                        testBus.getId(), "E5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bus_id").value(testBus.getId()))
                .andExpect(jsonPath("$.number").value("E5"))
                .andExpect(jsonPath("$.type").value("STANDARD"));
    }

    @Test
    void testGetSeatsByType_Success() throws Exception {
        // Given
        Bus bus2 = Bus.builder()
                .plate("XYZ789")
                .capacity(50)
                .status(BusStatus.AVAILABLE)
                .build();
        bus2 = busRepository.save(bus2);

        Seat preferentialSeat1 = Seat.builder()
                .bus(testBus)
                .number("P1")
                .type(SeatType.PREFERENTIAL)
                .build();

        Seat preferentialSeat2 = Seat.builder()
                .bus(testBus)
                .number("P2")
                .type(SeatType.PREFERENTIAL)
                .build();

        Seat preferentialSeat3 = Seat.builder()
                .bus(bus2)
                .number("P1")
                .type(SeatType.PREFERENTIAL)
                .build();

        Seat standardSeat = Seat.builder()
                .bus(testBus)
                .number("S1")
                .type(SeatType.STANDARD)
                .build();

        seatRepository.save(preferentialSeat1);
        seatRepository.save(preferentialSeat2);
        seatRepository.save(preferentialSeat3);
        seatRepository.save(standardSeat);

        // When & Then
        mockMvc.perform(get("/api/v1/seats/by-type/{type}", "PREFERENTIAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].type", everyItem(is("PREFERENTIAL"))))
                .andExpect(jsonPath("$[*].number", containsInAnyOrder("P1", "P2", "P1")));
    }

    @Test
    void testGetSeatsByType_StandardSeats_Success() throws Exception {
        // Given
        Seat standardSeat1 = Seat.builder()
                .bus(testBus)
                .number("S1")
                .type(SeatType.STANDARD)
                .build();

        Seat standardSeat2 = Seat.builder()
                .bus(testBus)
                .number("S2")
                .type(SeatType.STANDARD)
                .build();

        Seat preferentialSeat = Seat.builder()
                .bus(testBus)
                .number("P1")
                .type(SeatType.PREFERENTIAL)
                .build();

        seatRepository.save(standardSeat1);
        seatRepository.save(standardSeat2);
        seatRepository.save(preferentialSeat);

        // When & Then
        mockMvc.perform(get("/api/v1/seats/by-type/{type}", "STANDARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].type", everyItem(is("STANDARD"))))
                .andExpect(jsonPath("$[*].number", containsInAnyOrder("S1", "S2")));
    }
}