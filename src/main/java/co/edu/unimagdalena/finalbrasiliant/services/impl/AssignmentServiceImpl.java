package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.AssignmentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Assignment;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.AssignmentRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.AssignmentService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.AssignmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepo;
    private final TripRepository tripRepo;
    private final UserRepository userRepo;
    private final AssignmentMapper mapper;

    @Override
    @Transactional
    public AssignmentResponse create(Long tripId, AssignmentCreateRequest request) {
        var trip = tripRepo.findById(tripId).orElseThrow(
                () -> new NotFoundException("Trip %d not found.".formatted(tripId))
        );
        if (assignmentRepo.findByTrip_Id(trip.getId()).isPresent())
            throw new AlreadyExistsException("Trip %d already has an assignment. Maybe you wanna update the assignment.".formatted(trip.getId()));

        var driver = userRepo.findById(request.driverId()).orElseThrow(
                () -> new NotFoundException("Driver %d not found.".formatted(request.driverId()))
        );
        if (assignmentRepo.driverHasAnotherAssignment(driver.getId(), trip.getDepartureAt()))
            throw new AlreadyExistsException("Driver %d already has been assigned to another trip.".formatted(driver.getId()));

        var dispatcher = userRepo.findById(request.dispatcherId()).orElseThrow(
                () -> new NotFoundException("Dispatcher %d not found.".formatted(request.dispatcherId()))
        );
        if (assignmentRepo.dispatcherHasAnotherAssignment(dispatcher.getId(), trip.getDepartureAt()))
            throw new AlreadyExistsException("Dispatcher %d already has been assigned to another trip.".formatted(dispatcher.getId()));

        var saved = assignmentRepo.save(Assignment.builder().trip(trip).driver(driver)
                .dispatcher(dispatcher).checkListOk(request.checkListOk()).build());

        return mapper.toResponse(saved);
    }

    @Override
    public AssignmentResponse get(Long id) {
        return assignmentRepo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Assignment %d not found".formatted(id)));
    }

    @Override
    @Transactional
    public AssignmentResponse update(Long id, AssignmentUpdateRequest request) {
        var assignment = assignmentRepo.findById(id).orElseThrow(() -> new NotFoundException("Assignment %d not found.".formatted(id)));
        mapper.patch(assignment, request);
        return mapper.toResponse(assignmentRepo.save(assignment));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        assignmentRepo.deleteById(id);
    }

    @Override
    public Page<AssignmentResponse> listByAssignedAt(OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        if (to.isBefore(from)) throw new IllegalArgumentException("End date can't be before start date.");
        return assignmentRepo.findByAssignedAtBetween(from, to, pageable).map(mapper::toResponse);
    }

    @Override
    public AssignmentResponse getByTrip(Long tripId) {
        tripRepo.findById(tripId).orElseThrow(() -> new NotFoundException("Trip %d not found.".formatted(tripId)));
        return assignmentRepo.findByTrip_Id(tripId).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Trip %d hasn't assignment.".formatted(tripId)));
    }

    @Override
    public List<AssignmentResponse> listByDriver(Long driverId) {
        userRepo.findById(driverId).orElseThrow(() -> new NotFoundException("Driver %d not found.".formatted(driverId)));
        return assignmentRepo.findAllByDriver_Id(driverId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<AssignmentResponse> listByCheckList(Boolean checkListOk) {
        return assignmentRepo.findByCheckListOk(checkListOk).stream().map(mapper::toResponse).toList();
    }
}
