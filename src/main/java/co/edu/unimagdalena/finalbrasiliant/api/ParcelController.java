package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ParcelDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.ParcelStatus;
import co.edu.unimagdalena.finalbrasiliant.services.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/parcels")
public class ParcelController {
    private final ParcelService service;

    @PostMapping
    public ResponseEntity<ParcelResponse> createParcel(@Valid @RequestBody ParcelCreateRequest request, UriComponentsBuilder uriBuilder) {
        var body = service.create(request);
        var location = uriBuilder.path("/api/v1/parcels/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    @PatchMapping("/{id}")
    public ResponseEntity<ParcelResponse> update(@PathVariable Long id, @Valid @RequestBody ParcelUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-code")
    public ResponseEntity<ParcelResponse> getByCode(@RequestParam String code) {
        return ResponseEntity.ok(service.getByCode(code));
    }

    @GetMapping("/by-sender")
    public ResponseEntity<List<ParcelResponse>> listBySender(@RequestParam String senderName) {
        return ResponseEntity.ok(service.listBySender(senderName));
    }

    @GetMapping("/by-receiver")
    public ResponseEntity<List<ParcelResponse>> listByReceiver(@RequestParam String receiverName) {
        return ResponseEntity.ok(service.listBySender(receiverName));
    }

    @GetMapping("/by-OTP")
    public ResponseEntity<ParcelResponse> getByOTP(@RequestParam String otp) {
        return ResponseEntity.ok(service.getByDeliveryOTP(otp));
    }

    @GetMapping("/by-stretch")
    public ResponseEntity<List<ParcelResponse>> listByStretch(@RequestParam(value = "fromStop", required = false) Long fromStop,
                                                              @RequestParam(value = "toStop", required = false) Long toStop) {
        return ResponseEntity.ok(service.listByStretch(fromStop, toStop));
    }

    @GetMapping("/by-status")
    public ResponseEntity<Page<ParcelResponse>> listByStatus(@RequestParam ParcelStatus status, Pageable pageable) {
        var page = service.listByStatus(status, pageable);
        return ResponseEntity.ok(page);
    }
}
