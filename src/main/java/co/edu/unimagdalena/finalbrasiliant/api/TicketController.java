package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TicketDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import co.edu.unimagdalena.finalbrasiliant.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class TicketController {
    private final TicketService service;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK', 'ROLE_PASSENGER')")
    @PostMapping("/trips/{tripId}/tickets")
    public ResponseEntity<TicketResponse> createTicket(@PathVariable Long tripId, @Valid @RequestBody TicketCreateRequest request,
                                                       UriComponentsBuilder uriBuilder) {
        var body = service.create(tripId, request);
        var location = uriBuilder.path("/api/v1/tickets/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<TicketResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK', 'ROLE_PASSENGER')")
    @PatchMapping("/tickets/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id, @Valid @RequestBody TicketUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK', 'ROLE_PASSENGER')")
    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER', 'ROLE_CLERK')")
    @GetMapping("/trips/{tripId}/tickets")
    public ResponseEntity<List<TicketResponse>> listByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(service.listByTrip(tripId));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER', 'ROLE_CLERK')")
    @GetMapping("/trips/{tripId}/tickets/by-seat")
    public ResponseEntity<TicketResponse> getByTripSeat(@PathVariable Long tripId, @RequestParam("seatNumber") String seatNumber) {
        return ResponseEntity.ok(service.getByTripSeat(tripId, seatNumber));
    }

    @GetMapping("/tickets/by-qrcode")
    public ResponseEntity<TicketResponse> getByQRCode(@RequestParam String qrCode) {
        return ResponseEntity.ok(service.getByQRCode(qrCode));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER', 'ROLE_CLERK')")
    @GetMapping("/tickets/by-status")
    public ResponseEntity<Page<TicketResponse>> listByStatus(@RequestParam TicketStatus status, Pageable pageable) {
        var page = service.listByStatus(status, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER', 'ROLE_CLERK')")
    @GetMapping("/tickets/by-payment")
    public ResponseEntity<Page<TicketResponse>> listByPaymentMethod(@RequestParam("payment") PaymentMethod payment, Pageable pageable) {
        var page = service.listByPaymentMethod(payment, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER', 'ROLE_CLERK')")
    @GetMapping("/tickets/by-created")
    public ResponseEntity<Page<TicketResponse>> listByCreated(@RequestParam("from") OffsetDateTime from, @RequestParam("to") OffsetDateTime to,
                                                              Pageable pageable) {
        var page = service.listByCreatedAt(from, to, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/tickets/by-passenger")
    public ResponseEntity<List<TicketResponse>> listByPassenger(@RequestParam("passengerId") Long passengerId){
        return ResponseEntity.ok(service.listByPassenger(passengerId));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_DRIVER', 'ROLE_CLERK')")
    @GetMapping("/tickets/by-stretch")
    public ResponseEntity<List<TicketResponse>> listByStretch(@RequestParam(value = "fromStop", required = false) Long fromStopId,
                                                              @RequestParam(value = "toStop", required = false) Long toStopId){
        return ResponseEntity.ok(service.listByStretch(fromStopId, toStopId));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK', 'ROLE_PASSENGER')")
    @PatchMapping("/tickets{id}/cancel")
    public ResponseEntity<TicketResponse> cancel(@PathVariable Long id){
        return ResponseEntity.ok(service.cancelTicket(id));
    }
}
