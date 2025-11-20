package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BaggageDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.services.BaggageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1")
public class BaggageController {
    private final BaggageService service;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    @PostMapping("/tickets/{ticketId}/baggages")
    public ResponseEntity<BaggageResponse> createBaggage(@PathVariable Long ticketId, @Valid @RequestBody BaggageCreateRequest request,
                                                         UriComponentsBuilder uriBuilder) {
        var body = service.create(ticketId, request);
        var location = uriBuilder.path("/api/v1/baggages/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/tickets/{ticketId}/baggages")
    public ResponseEntity<List<BaggageResponse>> listByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(service.listByTicket(ticketId));
    }

    @GetMapping("/baggages/{id}")
    public ResponseEntity<BaggageResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    @PatchMapping("/baggages/{id}")
    public ResponseEntity<BaggageResponse> update(@PathVariable Long id, @Valid @RequestBody BaggageUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    @DeleteMapping("/baggages/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/baggages")
    public ResponseEntity<BaggageResponse> getByTagCode(@RequestParam String tagCode) {
        return ResponseEntity.ok(service.getByTagCode(tagCode));
    }

    @GetMapping("/baggages/by-passenger")
    public ResponseEntity<List<BaggageResponse>> listByPassenger(@RequestParam Long passengerId) {
        return ResponseEntity.ok(service.listByPassenger(passengerId));
    }

    @GetMapping("/baggages/by-weight")
    public ResponseEntity<Page<BaggageResponse>> listByWeight(@RequestParam("weightKg") BigDecimal weightKg, @RequestParam("gtrThanEq") Boolean greaterThanEq,
                                                              @PageableDefault(sort = "weightKg", direction = Sort.Direction.DESC) Pageable pageable) {
        var page = service.listByWeightKg(weightKg, greaterThanEq, pageable);
        return ResponseEntity.ok(page);
    }
}
