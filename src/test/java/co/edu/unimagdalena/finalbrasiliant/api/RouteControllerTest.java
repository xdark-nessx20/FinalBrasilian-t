package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.RouteRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RouteControllerIntegrationTest extends BaseTest{
    @Autowired
    private RouteRepository routeRepository;

    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();
    }

    @Test
    void testCreateRoute_Success() throws Exception {
        // Given
        RouteCreateRequest request = new RouteCreateRequest(
                "RT001",
                "Santa Marta - Barranquilla",
                "Santa Marta",
                "Barranquilla",
                new BigDecimal("95.50"),
                120
        );

        // When & Then
        mvc.perform(post("/api/v1/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("RT001"))
                .andExpect(jsonPath("$.routeName").value("Santa Marta - Barranquilla"))
                .andExpect(jsonPath("$.origin").value("Santa Marta"))
                .andExpect(jsonPath("$.destination").value("Barranquilla"))
                .andExpect(jsonPath("$.distanceKM").value(95.50))
                .andExpect(jsonPath("$.durationMin").value(120));
    }

    @Test
    void testGetRoute_Success() throws Exception {
        // Given
        Route route = Route.builder()
                .code("RT002")
                .routeName("Cartagena - Bogotá")
                .origin("Cartagena")
                .destination("Bogotá")
                .distanceKM(new BigDecimal("650.00"))
                .durationMin(720)
                .build();
        Route savedRoute = routeRepository.save(route);

        // When & Then
        mvc.perform(get("/api/v1/routes/{id}", savedRoute.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRoute.getId()))
                .andExpect(jsonPath("$.code").value("RT002"))
                .andExpect(jsonPath("$.routeName").value("Cartagena - Bogotá"))
                .andExpect(jsonPath("$.origin").value("Cartagena"))
                .andExpect(jsonPath("$.destination").value("Bogotá"))
                .andExpect(jsonPath("$.distanceKM").value(650.00))
                .andExpect(jsonPath("$.durationMin").value(720));
    }

    @Test
    void testUpdateRoute_Success() throws Exception {
        // Given
        Route route = Route.builder()
                .code("RT003")
                .routeName("Medellín - Cali")
                .origin("Medellín")
                .destination("Cali")
                .distanceKM(new BigDecimal("420.00"))
                .durationMin(480)
                .build();
        Route savedRoute = routeRepository.save(route);

        RouteUpdateRequest updateRequest = new RouteUpdateRequest(
                "RT003-UPD",
                "Medellín - Cali Express",
                "Medellín",
                "Cali",
                new BigDecimal("410.00"),
                450
        );

        // When & Then
        mvc.perform(patch("/api/v1/routes/{id}", savedRoute.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRoute.getId()))
                .andExpect(jsonPath("$.code").value("RT003-UPD"))
                .andExpect(jsonPath("$.routeName").value("Medellín - Cali Express"))
                .andExpect(jsonPath("$.distanceKM").value(410.00))
                .andExpect(jsonPath("$.durationMin").value(450));
    }

    @Test
    void testDeleteRoute_Success() throws Exception {
        // Given
        Route route = Route.builder()
                .code("RT004")
                .routeName("Bucaramanga - Cúcuta")
                .origin("Bucaramanga")
                .destination("Cúcuta")
                .distanceKM(new BigDecimal("195.00"))
                .durationMin(240)
                .build();
        Route savedRoute = routeRepository.save(route);

        // When & Then
        mvc.perform(delete("/api/v1/routes/{id}", savedRoute.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetByCode_Success() throws Exception {
        // Given
        Route route = Route.builder()
                .code("RT005")
                .routeName("Pereira - Armenia")
                .origin("Pereira")
                .destination("Armenia")
                .distanceKM(new BigDecimal("45.00"))
                .durationMin(60)
                .build();
        routeRepository.save(route);

        // When & Then
        mvc.perform(get("/api/v1/routes/by-code")
                        .param("Code", "RT005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("RT005"))
                .andExpect(jsonPath("$.routeName").value("Pereira - Armenia"));
    }

    @Test
    void testGetByRouteName_Success() throws Exception {
        // Given
        Route route = Route.builder()
                .code("RT006")
                .routeName("Pasto - Ipiales")
                .origin("Pasto")
                .destination("Ipiales")
                .distanceKM(new BigDecimal("82.00"))
                .durationMin(90)
                .build();
        routeRepository.save(route);

        // When & Then
        mvc.perform(get("/api/v1/routes/by-route-name")
                        .param("name", "Pasto - Ipiales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("RT006"))
                .andExpect(jsonPath("$.routeName").value("Pasto - Ipiales"));
    }

    @Test
    void testGetByOrigin_Success() throws Exception {
        // Given
        Route route1 = Route.builder()
                .code("RT007")
                .routeName("Bogotá - Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("415.00"))
                .durationMin(480)
                .build();

        Route route2 = Route.builder()
                .code("RT008")
                .routeName("Bogotá - Cali")
                .origin("Bogotá")
                .destination("Cali")
                .distanceKM(new BigDecimal("450.00"))
                .durationMin(540)
                .build();

        Route route3 = Route.builder()
                .code("RT009")
                .routeName("Cartagena - Santa Marta")
                .origin("Cartagena")
                .destination("Santa Marta")
                .distanceKM(new BigDecimal("225.00"))
                .durationMin(270)
                .build();

        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When & Then
        mvc.perform(get("/api/v1/routes/by-origin")
                        .param("origin", "Bogotá"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].origin", everyItem(is("Bogotá"))))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("RT007", "RT008")));
    }

    @Test
    void testGetByDestination_Success() throws Exception {
        // Given
        Route route1 = Route.builder()
                .code("RT010")
                .routeName("Bogotá - Barranquilla")
                .origin("Bogotá")
                .destination("Barranquilla")
                .distanceKM(new BigDecimal("985.00"))
                .durationMin(900)
                .build();

        Route route2 = Route.builder()
                .code("RT011")
                .routeName("Medellín - Barranquilla")
                .origin("Medellín")
                .destination("Barranquilla")
                .distanceKM(new BigDecimal("700.00"))
                .durationMin(720)
                .build();

        Route route3 = Route.builder()
                .code("RT012")
                .routeName("Cali - Buenaventura")
                .origin("Cali")
                .destination("Buenaventura")
                .distanceKM(new BigDecimal("125.00"))
                .durationMin(180)
                .build();

        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When & Then
        mvc.perform(get("/api/v1/routes/by-destination")
                        .param("destination", "Barranquilla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].destination", everyItem(is("Barranquilla"))))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("RT010", "RT011")));
    }

    @Test
    void testGetByDurationGreaterThan_Success() throws Exception {
        // Given
        Route route1 = Route.builder()
                .code("RT013")
                .routeName("Long Route 1")
                .origin("City A")
                .destination("City B")
                .distanceKM(new BigDecimal("500.00"))
                .durationMin(360)
                .build();

        Route route2 = Route.builder()
                .code("RT014")
                .routeName("Long Route 2")
                .origin("City C")
                .destination("City D")
                .distanceKM(new BigDecimal("600.00"))
                .durationMin(480)
                .build();

        Route route3 = Route.builder()
                .code("RT015")
                .routeName("Short Route")
                .origin("City E")
                .destination("City F")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When & Then
        mvc.perform(get("/api/v1/routes/by-duration-greater")
                        .param("minDuration", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].durationMin", everyItem(greaterThan(300))));
    }

    @Test
    void testGetByDurationBetween_Success() throws Exception {
        // Given
        Route route1 = Route.builder()
                .code("RT016")
                .routeName("Route 200 min")
                .origin("City A")
                .destination("City B")
                .distanceKM(new BigDecimal("200.00"))
                .durationMin(200)
                .build();

        Route route2 = Route.builder()
                .code("RT017")
                .routeName("Route 300 min")
                .origin("City C")
                .destination("City D")
                .distanceKM(new BigDecimal("300.00"))
                .durationMin(300)
                .build();

        Route route3 = Route.builder()
                .code("RT018")
                .routeName("Route 500 min")
                .origin("City E")
                .destination("City F")
                .distanceKM(new BigDecimal("500.00"))
                .durationMin(500)
                .build();

        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When & Then
        mvc.perform(get("/api/v1/routes/by-duration-between")
                        .param("min", "150")
                        .param("max", "350"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("RT016", "RT017")));
    }

    @Test
    void testGetByDistanceLessThan_Success() throws Exception {
        // Given
        Route route1 = Route.builder()
                .code("RT019")
                .routeName("Short Distance 1")
                .origin("City A")
                .destination("City B")
                .distanceKM(new BigDecimal("50.00"))
                .durationMin(60)
                .build();

        Route route2 = Route.builder()
                .code("RT020")
                .routeName("Short Distance 2")
                .origin("City C")
                .destination("City D")
                .distanceKM(new BigDecimal("80.00"))
                .durationMin(90)
                .build();

        Route route3 = Route.builder()
                .code("RT021")
                .routeName("Long Distance")
                .origin("City E")
                .destination("City F")
                .distanceKM(new BigDecimal("200.00"))
                .durationMin(240)
                .build();

        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When & Then
        mvc.perform(get("/api/v1/routes/by-distance-lesser")
                        .param("maxDistance", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("RT019", "RT020")));
    }

    @Test
    void testGetByDistanceBetween_Success() throws Exception {
        // Given
        Route route1 = Route.builder()
                .code("RT022")
                .routeName("Medium Distance 1")
                .origin("City A")
                .destination("City B")
                .distanceKM(new BigDecimal("100.00"))
                .durationMin(120)
                .build();

        Route route2 = Route.builder()
                .code("RT023")
                .routeName("Medium Distance 2")
                .origin("City C")
                .destination("City D")
                .distanceKM(new BigDecimal("150.00"))
                .durationMin(180)
                .build();

        Route route3 = Route.builder()
                .code("RT024")
                .routeName("Long Distance")
                .origin("City E")
                .destination("City F")
                .distanceKM(new BigDecimal("300.00"))
                .durationMin(360)
                .build();

        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When & Then
        mvc.perform(get("/api/v1/routes/by-distance-between")
                        .param("min", "90.00")
                        .param("max", "200.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("RT022", "RT023")));
    }

    @Test
    void testGetByOriginAndDestination_Success() throws Exception {
        // Given
        Route route1 = Route.builder()
                .code("RT025")
                .routeName("Santa Marta - Cartagena Route 1")
                .origin("Santa Marta")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("220.00"))
                .durationMin(240)
                .build();

        Route route2 = Route.builder()
                .code("RT026")
                .routeName("Santa Marta - Cartagena Route 2")
                .origin("Santa Marta")
                .destination("Cartagena")
                .distanceKM(new BigDecimal("225.00"))
                .durationMin(250)
                .build();

        Route route3 = Route.builder()
                .code("RT027")
                .routeName("Different Route")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("415.00"))
                .durationMin(480)
                .build();

        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When & Then
        mvc.perform(get("/api/v1/routes/by-origin-and-destination")
                        .param("origin", "Santa Marta")
                        .param("destination", "Cartagena")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].origin", everyItem(is("Santa Marta"))))
                .andExpect(jsonPath("$.content[*].destination", everyItem(is("Cartagena"))))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
    
    @Test
    void testGetAllRoutes_Success() throws Exception {
        // Given
        Route route1 = Route.builder()
                .code("RT028")
                .routeName("Santa Marta - Barranquilla")
                .origin("Santa Marta")
                .destination("Barranquilla")
                .distanceKM(new BigDecimal("95.50"))
                .durationMin(120)
                .build();

        Route route2 = Route.builder()
                .code("RT029")
                .routeName("Bogotá - Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(new BigDecimal("415.00"))
                .durationMin(480)
                .build();

        Route route3 = Route.builder()
                .code("RT030")
                .routeName("Cartagena - Santa Marta")
                .origin("Cartagena")
                .destination("Santa Marta")
                .distanceKM(new BigDecimal("220.00"))
                .durationMin(240)
                .build();

        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When & Then
        mvc.perform(get("/api/v1/routes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].code", containsInAnyOrder("RT028", "RT029", "RT030")));
    }
}