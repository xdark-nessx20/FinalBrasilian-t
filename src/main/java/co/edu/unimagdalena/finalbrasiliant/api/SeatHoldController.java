package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatHoldDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalbrasiliant.services.SeatHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1")
public class SeatHoldController {

    private final SeatHoldService service;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK', 'ROLE_PASSENGER')")
    @PostMapping("/trips/{tripId}/seats-hold")
    public ResponseEntity<SeatHoldResponse> createSeatHold(@PathVariable Long tripId,
                                                           @Valid @RequestBody SeatHoldCreateRequest request,
                                                           UriComponentsBuilder uriBuilder) {
        var body = service.create(request);
        var location = uriBuilder.path("/api/v1/seat-holds/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/seat-holds/{id}")
    public ResponseEntity<SeatHoldResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK', 'ROLE_PASSENGER')")
    @PatchMapping("/seat-holds/{id}")
    public ResponseEntity<SeatHoldResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody SeatHoldUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK', 'ROLE_PASSENGER')")
    @DeleteMapping("/seat-holds/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seat-holds/by-passenger")
    public ResponseEntity<List<SeatHoldResponse>> listByPassenger(@RequestParam Long passengerId) {
        return ResponseEntity.ok(service.listByPassenger(passengerId));
    }

    @GetMapping("/trips/{tripId}/seat-holds")
    public ResponseEntity<List<SeatHoldResponse>> listByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(service.listByTrip(tripId));
    }

    @GetMapping("/seat-holds/by-status")
    public ResponseEntity<List<SeatHoldResponse>> listByStatus(@RequestParam SeatHoldStatus status) {
        return ResponseEntity.ok(service.listByStatus(status));
    }

    @GetMapping("/trips/{tripId}/seat-holds/by-passenger")
    public ResponseEntity<List<SeatHoldResponse>> listByTripAndPassenger(@PathVariable Long tripId,
                                                                         @RequestParam Long passengerId,
                                                                         @RequestParam SeatHoldStatus status) {
        return ResponseEntity.ok(service.listByTripAndPassenger(tripId, passengerId, status));
    }

    @GetMapping("/seat-holds/expired")
    public ResponseEntity<List<SeatHoldResponse>> listExpiredHolds() {
        return ResponseEntity.ok(service.listExpiredHolds());
    }
}
