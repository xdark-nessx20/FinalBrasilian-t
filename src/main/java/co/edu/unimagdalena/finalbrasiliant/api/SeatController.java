package co.edu.unimagdalena.finalbrasiliant.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import co.edu.unimagdalena.finalbrasiliant.api.dto.AssignmentDTOs.AssignmentResponse;
import co.edu.unimagdalena.finalbrasiliant.api.dto.BaggageDTOs.BaggageCreateRequest;
import co.edu.unimagdalena.finalbrasiliant.api.dto.BaggageDTOs.BaggageResponse;
import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.SeatCreateRequest;
import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.SeatResponse;
import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.SeatUpdateRequest;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import co.edu.unimagdalena.finalbrasiliant.services.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1")
public class SeatController {
	private final SeatService service;
	
	@PostMapping("/buses/{busId}/seats")
    public ResponseEntity<SeatResponse> createSeat(@PathVariable Long busId, @Valid @RequestBody SeatCreateRequest request,
                                                         UriComponentsBuilder uriBuilder) {
        var body = service.create(busId, request);
        var location = uriBuilder.path("/api/v1/buses/{busId}/seats/{id}").buildAndExpand(busId, body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/seats/{id}")
    public ResponseEntity<SeatResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/seats/{id}")
    public ResponseEntity<SeatResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SeatUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/seats/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/buses/{busId}/seats")
    public ResponseEntity<List<SeatResponse>> getByBus(@PathVariable Long busId) {
    	return ResponseEntity.ok(service.getSeatsByBus(busId));
    }
    
    @GetMapping("/buses/{busId}/seats/by-number/{number}")
    public ResponseEntity<SeatResponse> getSeatByNumberAndBus(
            @PathVariable Long busId,
            @PathVariable String number) {
        return ResponseEntity.ok(service.getSeatByNumberAndBus(number, busId));
    }
    
    @GetMapping("/seats/by-type/{type}")
    public ResponseEntity<List<SeatResponse>> getByType(@PathVariable SeatType type) {
    	return ResponseEntity.ok(service.getSeatsByType(type));
    }
    
}
