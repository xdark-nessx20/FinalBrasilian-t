package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.AssignmentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Assignment;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.AssignmentRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.impl.AssignmentServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.AssignmentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock
    private AssignmentRepository assignmentRepo;

    @Mock
    private TripRepository tripRepo;

    @Mock
    private UserRepository userRepo;

    @Spy
    private AssignmentMapper mapper = Mappers.getMapper(AssignmentMapper.class);

    @InjectMocks
    private AssignmentServiceImpl service;

    @Test
    void shouldCreateAndReturnResponse() {
        // Given
        OffsetDateTime departureAt = OffsetDateTime.now().plusDays(1);

        var trip = Trip.builder()
                .id(1L)
                .departureAt(departureAt)
                .build();

        var driver = User.builder()
                .id(2L)
                .userName("Juan Driver")
                .build();

        var dispatcher = User.builder()
                .id(3L)
                .userName("Carlos Dispatcher")
                .build();

        var request = new AssignmentCreateRequest(1L, 2L, 3L, true);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepo.findByTrip_Id(1L)).thenReturn(Optional.empty());
        when(userRepo.findById(2L)).thenReturn(Optional.of(driver));
        when(assignmentRepo.driverHasAnotherAssigment(2L, departureAt)).thenReturn(false);
        when(userRepo.findById(3L)).thenReturn(Optional.of(dispatcher));
        when(assignmentRepo.dispatcherHasAnotherAssigment(3L, departureAt)).thenReturn(false);

        when(assignmentRepo.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment a = inv.getArgument(0);
            a.setId(10L);
            a.setAssignedAt(OffsetDateTime.now());
            return a;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.driver().id()).isEqualTo(2L);
        assertThat(response.driver().userName()).isEqualTo("Juan Driver");
        assertThat(response.dispatcher().id()).isEqualTo(3L);
        assertThat(response.dispatcher().userName()).isEqualTo("Carlos Dispatcher");
        assertThat(response.checkListOk()).isTrue();
        assertThat(response.assignedAt()).isNotNull();

        verify(assignmentRepo).save(any(Assignment.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripNotExists() {
        // Given
        var request = new AssignmentCreateRequest(99L, 2L, 3L, true);
        when(tripRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");

        verify(assignmentRepo, never()).save(any());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenTripAlreadyHasAssignment() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var existingAssignment = Assignment.builder().id(5L).trip(trip).build();
        var request = new AssignmentCreateRequest(1L, 2L, 3L, true);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepo.findByTrip_Id(1L)).thenReturn(Optional.of(existingAssignment));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Trip 1 already has an assignment");

        verify(assignmentRepo, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDriverNotExists() {
        // Given
        var trip = Trip.builder().id(1L).departureAt(OffsetDateTime.now()).build();
        var request = new AssignmentCreateRequest(1L, 99L, 3L, true);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepo.findByTrip_Id(1L)).thenReturn(Optional.empty());
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Driver 99 not found");

        verify(assignmentRepo, never()).save(any());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenDriverHasAnotherAssignment() {
        // Given
        OffsetDateTime departureAt = OffsetDateTime.now().plusDays(1);
        var trip = Trip.builder().id(1L).departureAt(departureAt).build();
        var driver = User.builder().id(2L).userName("Juan").build();
        var request = new AssignmentCreateRequest(1L, 2L, 3L, true);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepo.findByTrip_Id(1L)).thenReturn(Optional.empty());
        when(userRepo.findById(2L)).thenReturn(Optional.of(driver));
        when(assignmentRepo.driverHasAnotherAssigment(2L, departureAt)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Driver 2 already has been assigned to another trip");

        verify(assignmentRepo, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDispatcherNotExists() {
        // Given
        OffsetDateTime departureAt = OffsetDateTime.now().plusDays(1);
        var trip = Trip.builder().id(1L).departureAt(departureAt).build();
        var driver = User.builder().id(2L).build();
        var request = new AssignmentCreateRequest(1L, 2L, 99L, true);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepo.findByTrip_Id(1L)).thenReturn(Optional.empty());
        when(userRepo.findById(2L)).thenReturn(Optional.of(driver));
        when(assignmentRepo.driverHasAnotherAssigment(2L, departureAt)).thenReturn(false);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Dispatcher 99 not found");

        verify(assignmentRepo, never()).save(any());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenDispatcherHasAnotherAssignment() {
        // Given
        OffsetDateTime departureAt = OffsetDateTime.now().plusDays(1);
        var trip = Trip.builder().id(1L).departureAt(departureAt).build();
        var driver = User.builder().id(2L).build();
        var dispatcher = User.builder().id(3L).build();
        var request = new AssignmentCreateRequest(1L, 2L, 3L, true);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepo.findByTrip_Id(1L)).thenReturn(Optional.empty());
        when(userRepo.findById(2L)).thenReturn(Optional.of(driver));
        when(assignmentRepo.driverHasAnotherAssigment(2L, departureAt)).thenReturn(false);
        when(userRepo.findById(3L)).thenReturn(Optional.of(dispatcher));
        when(assignmentRepo.dispatcherHasAnotherAssigment(3L, departureAt)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Dispatcher 3 already has been assigned to another trip");

        verify(assignmentRepo, never()).save(any());
    }

    @Test
    void shouldGetAssignmentById() {
        // Given
        var assignment = Assignment.builder()
                .id(10L)
                .trip(Trip.builder().id(1L).build())
                .driver(User.builder().id(2L).userName("Juan").build())
                .dispatcher(User.builder().id(3L).userName("Carlos").build())
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build();

        when(assignmentRepo.findById(10L)).thenReturn(Optional.of(assignment));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.driver().userName()).isEqualTo("Juan");
        assertThat(response.dispatcher().userName()).isEqualTo("Carlos");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentAssignment() {
        // Given
        when(assignmentRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Assignment 99 not found");
    }

    @Test
    void shouldUpdateAssignmentViaPatch() {
        // Given
        var assignment = Assignment.builder()
                .id(10L)
                .trip(Trip.builder().id(1L).build())
                .driver(User.builder().id(2L).userName("Juan").build())
                .dispatcher(User.builder().id(3L).userName("Carlos").build())
                .checkListOk(false)
                .assignedAt(OffsetDateTime.now())
                .build();

        var updateRequest = new AssignmentUpdateRequest(true, null, null);

        when(assignmentRepo.findById(10L)).thenReturn(Optional.of(assignment));
        when(assignmentRepo.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.checkListOk()).isTrue();
        verify(assignmentRepo).save(any(Assignment.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentAssignment() {
        // Given
        var updateRequest = new AssignmentUpdateRequest(true, null, null);
        when(assignmentRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Assignment 99 not found");

        verify(assignmentRepo, never()).save(any());
    }

    @Test
    void shouldDeleteAssignment() {
        // When
        service.delete(10L);

        // Then
        verify(assignmentRepo).deleteById(10L);
    }

    @Test
    void shouldListAssignmentsByAssignedAtBetweenDates() {
        // Given
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        var assignment1 = Assignment.builder()
                .id(1L)
                .trip(Trip.builder().id(1L).build())
                .driver(User.builder().id(2L).userName("Juan").build())
                .dispatcher(User.builder().id(3L).userName("Carlos").build())
                .assignedAt(from.plusDays(1))
                .build();

        var assignment2 = Assignment.builder()
                .id(2L)
                .trip(Trip.builder().id(2L).build())
                .driver(User.builder().id(4L).userName("Ana").build())
                .dispatcher(User.builder().id(5L).userName("Luis").build())
                .assignedAt(from.plusDays(3))
                .build();

        var page = new PageImpl<>(List.of(assignment1, assignment2));
        when(assignmentRepo.findByAssignedAtBetween(from, to, pageable)).thenReturn(page);

        // When
        var result = service.listByAssignedAt(from, to, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(1).id()).isEqualTo(2L);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenToDateIsBeforeFromDate() {
        // Given
        OffsetDateTime from = OffsetDateTime.now();
        OffsetDateTime to = from.minusDays(1);
        Pageable pageable = PageRequest.of(0, 10);

        // When / Then
        assertThatThrownBy(() -> service.listByAssignedAt(from, to, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date can't be before start date");

        verify(assignmentRepo, never()).findByAssignedAtBetween(any(), any(), any());
    }

    @Test
    void shouldGetAssignmentByTrip() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var assignment = Assignment.builder()
                .id(10L)
                .trip(trip)
                .driver(User.builder().id(2L).userName("Juan").build())
                .dispatcher(User.builder().id(3L).userName("Carlos").build())
                .assignedAt(OffsetDateTime.now())
                .build();

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepo.findByTrip_Id(1L)).thenReturn(Optional.of(assignment));

        // When
        var response = service.getByTrip(1L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tripId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetByNonExistentTrip() {
        // Given
        when(tripRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByTrip(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripHasNoAssignment() {
        // Given
        var trip = Trip.builder().id(1L).build();
        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepo.findByTrip_Id(1L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByTrip(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 1 hasn't assignment");
    }

    @Test
    void shouldListAssignmentsByDriver() {
        // Given
        var driver = User.builder().id(2L).userName("Juan").build();
        var assignment1 = Assignment.builder()
                .id(1L)
                .trip(Trip.builder().id(1L).build())
                .driver(driver)
                .dispatcher(User.builder().id(3L).userName("Carlos").build())
                .build();

        var assignment2 = Assignment.builder()
                .id(2L)
                .trip(Trip.builder().id(2L).build())
                .driver(driver)
                .dispatcher(User.builder().id(4L).userName("Luis").build())
                .build();

        when(userRepo.findById(2L)).thenReturn(Optional.of(driver));
        when(assignmentRepo.findAllByDriver_Id(2L)).thenReturn(List.of(assignment1, assignment2));

        // When
        var result = service.listByDriver(2L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).driver().id()).isEqualTo(2L);
        assertThat(result.get(1).driver().id()).isEqualTo(2L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenListByNonExistentDriver() {
        // Given
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.listByDriver(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Driver 99 not found");
    }

    @Test
    void shouldListAssignmentsByCheckListOk() {
        // Given
        var assignment1 = Assignment.builder()
                .id(1L)
                .trip(Trip.builder().id(1L).build())
                .driver(User.builder().id(2L).userName("Juan").build())
                .dispatcher(User.builder().id(3L).userName("Carlos").build())
                .checkListOk(true)
                .build();

        var assignment2 = Assignment.builder()
                .id(2L)
                .trip(Trip.builder().id(2L).build())
                .driver(User.builder().id(4L).userName("Ana").build())
                .dispatcher(User.builder().id(5L).userName("Luis").build())
                .checkListOk(true)
                .build();

        when(assignmentRepo.findByCheckListOk(true)).thenReturn(List.of(assignment1, assignment2));

        // When
        var result = service.listByCheckList(true);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.checkListOk() == true);
    }

    @Test
    void shouldReturnEmptyListWhenNoAssignmentsMatchCheckList() {
        // Given
        when(assignmentRepo.findByCheckListOk(false)).thenReturn(List.of());

        // When
        var result = service.listByCheckList(false);

        // Then
        assertThat(result).isEmpty();
    }
}
