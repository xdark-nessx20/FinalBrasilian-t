package co.edu.unimagdalena.finalbrasiliant.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.RouteCreateRequest;
import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.RouteResponse;
import co.edu.unimagdalena.finalbrasiliant.api.dto.RouteDTO.RouteUpdateRequest;
import co.edu.unimagdalena.finalbrasiliant.services.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/routes")
public class RouteController {
	private final RouteService service;
	
	@PostMapping
    public ResponseEntity<RouteResponse> createRoute(
            @Valid @RequestBody RouteCreateRequest request,
            UriComponentsBuilder uriBuilder) {
        var body = service.create(request);
        var location = uriBuilder.path("/api/v1/routes/{id}")
                                 .buildAndExpand(body.id())
                                 .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RouteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RouteUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
	
    @GetMapping("/by-code")
    public ResponseEntity<RouteResponse> getByCode(@RequestParam String Code) {
        return ResponseEntity.ok(service.getByCode(Code));
    }
    
    @GetMapping("/by-route-name")
    public ResponseEntity<RouteResponse> getByRouteName(@RequestParam String name) {
        return ResponseEntity.ok(service.getByRouteName(name));
    }
    
    @GetMapping("/by-origin")
    public ResponseEntity<List<RouteResponse>> getByOrigin(@RequestParam String origin){
        return ResponseEntity.ok(service.getByOrigin(origin));
    }
    
    @GetMapping("/by-destination")
    public ResponseEntity<List<RouteResponse>> getByDestination(@RequestParam String destination){
        return ResponseEntity.ok(service.getByDestination(destination));
    }
    
    @GetMapping("/by-duration-greater")
    public ResponseEntity<List<RouteResponse>> getByDurationGreaterThan(@RequestParam Integer minDuration){
        return ResponseEntity.ok(service.getByMinDurationGreaterThan(minDuration));
    }
    
    @GetMapping("/by-duration-between")
    public ResponseEntity<List<RouteResponse>> getByDurationBetween(@RequestParam Integer min, @RequestParam Integer max){
        return ResponseEntity.ok(service.getByDurationBetween(min, max));
    }
    
    @GetMapping("/by-distance-lesser")
    public ResponseEntity<List<RouteResponse>> getByDistancelessThan(@RequestParam BigDecimal maxDistance){
        return ResponseEntity.ok(service.getByDistanceLessThan(maxDistance));
    }
    
    @GetMapping("/by-distance-between")
    public ResponseEntity<List<RouteResponse>> getByDistanceBetween(@RequestParam BigDecimal min, @RequestParam BigDecimal max){
        return ResponseEntity.ok(service.getByDistanceBetween(min, max));
    }
    
    @GetMapping("/by-origin-and-destination")
    public ResponseEntity<Page<RouteResponse>> getByOriginAndDestination(@RequestParam String origin, 
    		@RequestParam String destination,Pageable pageable) {
        var page = service.getByOriginAndDestination(origin, destination, pageable);
        return ResponseEntity.ok(page);
    }
}
