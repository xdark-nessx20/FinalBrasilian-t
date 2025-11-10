package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.AssignmentDTOs.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface AssignmentService {
    AssignmentResponse create(AssignmentCreateRequest request);
    AssignmentResponse get(Long id);
    AssignmentResponse update(Long id, AssignmentUpdateRequest request);
    void delete(Long id);

    Page<AssignmentResponse> listByAssignedAt(OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    AssignmentResponse getByTrip(Long tripId);
    List<AssignmentResponse> listByDriver(Long driverId);
    List<AssignmentResponse> listByCheckList(Boolean checkListOk);
}
