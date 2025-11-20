package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.AssignmentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.services.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1")
public class AssignmentController {
    private final AssignmentService service;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/trips/{tripId}/assignment")
    public ResponseEntity<AssignmentResponse> createAssignment(@PathVariable Long tripId, @Valid @RequestBody AssignmentCreateRequest request,
                                                               UriComponentsBuilder uriBuilder) {
        var body = service.create(tripId, request);
        var location =  uriBuilder.path("/api/v1/assignments/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/trips/{tripId}/assignment")
    public ResponseEntity<AssignmentResponse> getByTrip(@PathVariable Long tripId) {
        var body = service.getByTrip(tripId);
        return ResponseEntity.ok(body);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER')")
    @GetMapping("/assignments/{id}")
    public ResponseEntity<AssignmentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    @PatchMapping("/assignments/{id}")
    public ResponseEntity<AssignmentResponse> update(@PathVariable Long id, @Valid @RequestBody AssignmentUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER')")
    @GetMapping("/assignments/by-date")
    public ResponseEntity<Page<AssignmentResponse>> listByDate(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
                                                               @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end, Pageable pageable){
        var page = service.listByAssignedAt(start, end, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER')")
    @GetMapping("/assignments/by-driver")
    public ResponseEntity<List<AssignmentResponse>> listByDriver(@RequestParam Long driverId){
        var list = service.listByDriver(driverId);
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER')")
    @GetMapping("/assignments/by-checklist")
    public ResponseEntity<List<AssignmentResponse>> listByCheckList(@RequestParam Boolean checkList){
        var list = service.listByCheckList(checkList);
        return ResponseEntity.ok(list);
    }
}
