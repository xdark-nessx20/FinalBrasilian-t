package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TripRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TripControllerIntegrationTest extends BaseTest{
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    private Route testRoute;
    private Bus testBus;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        busRepository.deleteAll();
        routeRepository.deleteAll();

        // Create test route
        testRoute = Route.builder()
                .code("RT001")
                .routeName("Santa Marta - Barranquilla")
                .origin("Santa Marta")
                .destination("Barranquilla")
                .distanceKM(new BigDecimal("95.50"))
                .durationMin(120)
                .build();
        testRoute = routeRepository.save(testRoute);

        // Create test bus
        testBus = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(BusStatus.AVAILABLE)
                .build();
        testBus = busRepository.save(testBus);
    }

    @Test
    void testCreateTrip_Success() throws Exception {
        // Given
        LocalDate tripDate = LocalDate.now().plusDays(1);
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0);
        OffsetDateTime arrival = departure.plusHours(2);

        TripCreateRequest request = new TripCreateRequest(
                testBus.getId(),
                tripDate,
                departure,
                arrival
        );

        // When & Then
        mvc.perform(post("/api/v1/routes/{routeId}/trips", testRoute.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.route_id").value(testRoute.getId()))
                .andExpect(jsonPath("$.bus_id").value(testBus.getId()))
                .andExpect(jsonPath("$.date").value(tripDate.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void testGetTrip_Success() throws Exception {
        // Given
        LocalDate tripDate = LocalDate.now();
        OffsetDateTime departure = OffsetDateTime.now().withHour(10).withMinute(0);
        OffsetDateTime arrival = departure.plusHours(2);

        Trip trip = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(tripDate)
                .departureAt(departure)
                .arrivalETA(arrival)
                .status(TripStatus.SCHEDULED)
                .build();
        Trip savedTrip = tripRepository.save(trip);

        // When & Then
        mvc.perform(get("/api/v1/trips/{tripId}", savedTrip.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTrip.getId()))
                .andExpect(jsonPath("$.route_id").value(testRoute.getId()))
                .andExpect(jsonPath("$.bus_id").value(testBus.getId()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void testUpdateTrip_Success() throws Exception {
        // Given
        LocalDate tripDate = LocalDate.now();
        OffsetDateTime departure = OffsetDateTime.now().withHour(10).withMinute(0);
        OffsetDateTime arrival = departure.plusHours(2);

        Trip trip = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(tripDate)
                .departureAt(departure)
                .arrivalETA(arrival)
                .status(TripStatus.SCHEDULED)
                .build();
        Trip savedTrip = tripRepository.save(trip);

        LocalDate newDate = LocalDate.now().plusDays(1);
        OffsetDateTime newDeparture = OffsetDateTime.now().plusDays(1).withHour(14).withMinute(0);
        OffsetDateTime newArrival = newDeparture.plusHours(2);

        TripUpdateRequest updateRequest = new TripUpdateRequest(
                null,
                testBus.getId(),
                newDate,
                newDeparture,
                newArrival,
                TripStatus.BOARDING
        );

        // When & Then
        mvc.perform(patch("/api/v1/trips/{tripId}", savedTrip.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTrip.getId()))
                .andExpect(jsonPath("$.date").value(newDate.toString()))
                .andExpect(jsonPath("$.status").value("BOARDING"));
    }

    @Test
    void testDeleteTrip_Success() throws Exception {
        // Given
        Trip trip = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();
        Trip savedTrip = tripRepository.save(trip);

        // When & Then
        mvc.perform(delete("/api/v1/trips/{tripId}", savedTrip.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetByRouteId_Success() throws Exception {
        // Given
        Trip trip1 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip trip2 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now().plusDays(1))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);

        // When & Then
        mvc.perform(get("/api/v1/routes/{routeId}/trips", testRoute.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].route_id", everyItem(is(testRoute.getId().intValue()))))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testGetByBusId_Success() throws Exception {
        // Given
        Trip trip1 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip trip2 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now().plusDays(1))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);

        // When & Then
        mvc.perform(get("/api/v1/trips/by-bus/{busId}", testBus.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].bus_id", everyItem(is(testBus.getId().intValue()))))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testGetByDepartureBetween_Success() throws Exception {
        // Given
        OffsetDateTime start = OffsetDateTime.now().minusHours(1);
        OffsetDateTime end = OffsetDateTime.now().plusDays(2);

        Trip trip1 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip trip2 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now().plusDays(1))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);

        // When & Then
        mvc.perform(get("/api/v1/trips/by-departure")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void testGetByArrivalBetween_Success() throws Exception {
        // Given
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = OffsetDateTime.now().plusDays(2);

        Trip trip1 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip trip2 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now().plusDays(1))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);

        // When & Then
        mvc.perform(get("/api/v1/trips/by-arrival")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void testGetByStatus_Success() throws Exception {
        // Given
        Trip scheduledTrip1 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip scheduledTrip2 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now().plusDays(1))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip departedTrip = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now().minusHours(1))
                .arrivalETA(OffsetDateTime.now().plusHours(1))
                .status(TripStatus.DEPARTED)
                .build();

        tripRepository.save(scheduledTrip1);
        tripRepository.save(scheduledTrip2);
        tripRepository.save(departedTrip);

        // When & Then
        mvc.perform(get("/api/v1/trips/by-status")
                        .param("status", "SCHEDULED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("SCHEDULED"))))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testGetByRouteIdAndStatus_Success() throws Exception {
        // Given
        Trip scheduledTrip = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip departedTrip = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now().minusHours(1))
                .arrivalETA(OffsetDateTime.now().plusHours(1))
                .status(TripStatus.DEPARTED)
                .build();

        tripRepository.save(scheduledTrip);
        tripRepository.save(departedTrip);

        // When & Then
        mvc.perform(get("/api/v1/trips/search")
                        .param("routeId", testRoute.getId().toString())
                        .param("status", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].route_id").value(testRoute.getId().intValue()))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    void testGetByDate_Success() throws Exception {
        // Given
        LocalDate targetDate = LocalDate.now();

        Trip trip1 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(targetDate)
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip trip2 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(targetDate)
                .departureAt(OffsetDateTime.now().plusHours(4))
                .arrivalETA(OffsetDateTime.now().plusHours(6))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip trip3 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now().plusDays(1))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);

        // When & Then
        mvc.perform(get("/api/v1/trips/by-date")
                        .param("date", targetDate.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].date", everyItem(is(targetDate.toString()))))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
    
    @Test
    void testGetByRouteIdAndDate_Success() throws Exception {
        // Given
        LocalDate targetDate = LocalDate.now();

        Trip trip1 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(targetDate)
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        Trip trip2 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(targetDate)
                .departureAt(OffsetDateTime.now().plusHours(4))
                .arrivalETA(OffsetDateTime.now().plusHours(6))
                .status(TripStatus.DEPARTED)
                .build();

        // Trip with different route (should not be included)
        Route otherRoute = Route.builder()
                .code("RT002")
                .routeName("Barranquilla - Cartagena")
                .origin("Barranquilla")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("120.00"))
                .durationMin(90)
                .build();
        otherRoute = routeRepository.save(otherRoute);

        Trip trip3 = Trip.builder()
                .route(otherRoute)
                .bus(testBus)
                .date(targetDate)
                .departureAt(OffsetDateTime.now())
                .arrivalETA(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        // Trip with different date (should not be included)
        Trip trip4 = Trip.builder()
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now().plusDays(1))
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalETA(OffsetDateTime.now().plusDays(1).plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);
        tripRepository.save(trip4);

        // When & Then
        mvc.perform(get("/api/v1/trips/search")
                        .param("routeId", testRoute.getId().toString())
                        .param("date", targetDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].route_id", everyItem(is(testRoute.getId().intValue()))))
                .andExpect(jsonPath("$[*].date", everyItem(is(targetDate.toString()))));
    }
}