package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentRepositoryTest extends AbstractRepository {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    private User driver1;
    private Trip trip1;
    private Assignment assignment1;
    private Assignment assignment2;
    private Assignment assignment3;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();

        now = OffsetDateTime.now();

        Route route1 = routeRepository.save(Route.builder().code("ZZZZZZ").routeName("a")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(BigDecimal.valueOf(415.0))
                .durationMin(360)
                .build());

        Route route2 = routeRepository.save(Route.builder().code("ZZZZZY").routeName("b")
                .origin("Cali")
                .destination("Cartagena")
                .distanceKM(BigDecimal.valueOf(1100.0))
                .durationMin(900)
                .build());

        Bus bus1 = busRepository.save(Bus.builder()
                .plate("ABC123")
                .capacity(45)
                .build());

        Bus bus2 = busRepository.save(Bus.builder()
                .plate("XYZ789")
                .capacity(50)
                .build());

        driver1 = createUser("Juan Pérez", "juan@example.com", "3001234567", Role.DRIVER);
        User driver2 = createUser("María García", "maria@example.com", "3007654321", Role.DRIVER);
        User dispatcher1 = createUser("Carlos Admin", "carlos@example.com", "3009876543", Role.DISPATCHER);


        trip1 = createTrip(route1, bus1, now.plusDays(1), TripStatus.SCHEDULED);
        Trip trip2 = createTrip(route2, bus2, now.plusDays(2), TripStatus.DEPARTED);
        Trip trip3 = createTrip(route1, bus1, now.plusDays(3), TripStatus.ARRIVED);

        assignment1 = createAssignment(trip1, driver1, dispatcher1, true, now.minusDays(5));
        assignment2 = createAssignment(trip2, driver1, dispatcher1, false, now.minusDays(2));
        assignment3 = createAssignment(trip3, driver2, dispatcher1, true, now.minusDays(10));
    }

    private User createUser(String userName, String email, String phone, Role role) {
        return userRepository.save(User.builder()
                .userName(userName)
                .email(email)
                .phone(phone)
                .role(role)
                .status(true)
                .passwordHash("hashed_password")
                .build());
    }

    private Trip createTrip(Route route, Bus bus, OffsetDateTime departureAt, TripStatus status) {
        return tripRepository.save(Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureAt(departureAt)
                .arrivalETA(departureAt.plusHours(6))
                .status(status)
                .build());
    }

    private Assignment createAssignment(Trip trip, User driver, User dispatcher,
                                        Boolean checkListOk, OffsetDateTime assignedAt) {
        return assignmentRepository.save(Assignment.builder()
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checkListOk(checkListOk)
                .assignedAt(assignedAt)
                .build());
    }

    @Test
    void shouldFindAssignmentByTripId() {
        Optional<Assignment> result = assignmentRepository.findByTripId(trip1.getId());

        assertThat(result).isPresent();
        assertThat(result.get())
                .extracting(Assignment::getId, a -> a.getTrip().getId())
                .containsExactly(assignment1.getId(), trip1.getId());
    }

    @Test
    void shouldFindAllAssignmentsByDriverId() {
        List<Assignment> result = assignmentRepository.findAllByDriverId(driver1.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(Assignment::getId)
                .containsExactlyInAnyOrder(assignment1.getId(), assignment2.getId());
    }

    @Test
    void shouldFindAssignmentsByDateRange() {
        OffsetDateTime from = now.minusDays(6);
        OffsetDateTime to = now.minusDays(1);

        Page<Assignment> result = assignmentRepository.findByAssignedAtBetween(
                from, to, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(0);
    }

    @Test
    void shouldFindAssignmentsByCheckListOkTrue() {
        List<Assignment> result = assignmentRepository.findByCheckListOk(true);

        assertThat(result)
                .hasSize(2)
                .extracting(Assignment::getId)
                .containsExactlyInAnyOrder(assignment1.getId(), assignment3.getId())
                .allMatch(id -> result.stream()
                        .filter(a -> a.getId().equals(id))
                        .findFirst()
                        .map(Assignment::getCheckListOk)
                        .orElse(false));
    }

    @Test
    void shouldFindAssignmentByIdWithAllDetails() {
        Optional<Assignment> result = assignmentRepository.findByIdWithAllDetails(assignment1.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(assignment -> {
                    assertThat(assignment.getId()).isEqualTo(assignment1.getId());
                    assertThat(assignment.getTrip())
                            .isNotNull()
                            .satisfies(trip -> {
                                assertThat(trip.getId()).isEqualTo(trip1.getId());
                                assertThat(trip.getStatus()).isEqualTo(TripStatus.SCHEDULED);
                            });
                });
    }
}
