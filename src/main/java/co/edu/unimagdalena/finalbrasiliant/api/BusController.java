package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BusDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import co.edu.unimagdalena.finalbrasiliant.services.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;


@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/buses")

public class BusController {

    private final BusService service;

    @PostMapping
    public ResponseEntity<BusResponse> createBus(@Valid @RequestBody BusCreateRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        var body = service.create(request);
        var location = uriBuilder.path("/api/v1/buses/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BusResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody BusUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-plate")
    public ResponseEntity<BusResponse> getByPlate(@RequestParam String plate) {
        return ResponseEntity.ok(service.getByPlate(plate));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<BusResponse>> listByStatus(@RequestParam BusStatus status) {
        return ResponseEntity.ok(service.listByStatus(status));
    }

    @GetMapping("/by-capacity")
    public ResponseEntity<List<BusResponse>> listByCapacity(@RequestParam Integer capacity) {
        return ResponseEntity.ok(service.listByCapacityGreaterThanEqual(capacity));
    }

    @GetMapping("/by-amenities")
    public ResponseEntity<List<BusResponse>> listByAmenities(@RequestParam Set<String> amenities) {
        return ResponseEntity.ok(service.listByAmenities(amenities));
    }
}
