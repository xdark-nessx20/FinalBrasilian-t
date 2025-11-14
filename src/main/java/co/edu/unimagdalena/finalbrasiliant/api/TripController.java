package co.edu.unimagdalena.finalbrasiliant.api;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.TripCreateRequest;
import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.TripResponse;
import co.edu.unimagdalena.finalbrasiliant.api.dto.TripDTO.TripUpdateRequest;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import co.edu.unimagdalena.finalbrasiliant.services.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/trips")
public class TripController {
	private final TripService service;
	
	 @PostMapping
	    public ResponseEntity<TripResponse> createTrip(
	            @Valid @RequestBody TripCreateRequest request,
	            UriComponentsBuilder uriBuilder) {
	        var body = service.create(request);
	        var location = uriBuilder.path("/api/v1/trips/{id}")
	                                 .buildAndExpand(body.id())
	                                 .toUri();
	        return ResponseEntity.created(location).body(body);
	    }

	    @GetMapping("/{id}")
	    public ResponseEntity<TripResponse> get(@PathVariable Long id) {
	        return ResponseEntity.ok(service.get(id));
	    }

	    @PatchMapping("/{id}")
	    public ResponseEntity<TripResponse> update(
	            @PathVariable Long id,
	            @Valid @RequestBody TripUpdateRequest request) {
	        return ResponseEntity.ok(service.update(id, request));
	    }

	    @DeleteMapping("/{id}")
	    public ResponseEntity<Void> delete(@PathVariable Long id) {
	        service.delete(id);
	        return ResponseEntity.noContent().build();
	    }
	
	    @GetMapping("/by-route/{routeId}")
	    public ResponseEntity<Page<TripResponse>> getByRouteId(@PathVariable Long routeId, Pageable pageable) {
	        var page = service.getAllByRouteId(routeId, pageable);
	        return ResponseEntity.ok(page);
	    }
	    
	    @GetMapping("/by-bus/{busId}")
	    public ResponseEntity<Page<TripResponse>> getByBusId(@PathVariable Long busId, Pageable pageable) {
	        var page = service.getAllByBusId(busId, pageable);
	        return ResponseEntity.ok(page);
	    }
	    
	    @GetMapping("/by-departure")
	    public ResponseEntity<Page<TripResponse>> getByDepartureBetween(@RequestParam OffsetDateTime start, 
	    		@RequestParam OffsetDateTime end, Pageable pageable) {
	        var page = service.getByDepartureBetween(start, end, pageable);
	        return ResponseEntity.ok(page);
	    }
	    
	    @GetMapping("/by-arrival")
	    public ResponseEntity<Page<TripResponse>> getByArrivalBetween(@RequestParam OffsetDateTime start, 
	    		@RequestParam OffsetDateTime end, Pageable pageable) {
	        var page = service.getByArrivalBetween(start, end, pageable);
	        return ResponseEntity.ok(page);
	    }
	    
	    @GetMapping("/by-status")
	    public ResponseEntity<Page<TripResponse>> getByStatus(@RequestParam TripStatus status, Pageable pageable) {
	        var page = service.getByStatus(status, pageable);
	        return ResponseEntity.ok(page);
	    }
	    
	    @GetMapping("/search")
	    public ResponseEntity<List<TripResponse>> getByRouteIdAndStatus(@RequestParam Long routeId, @RequestParam TripStatus status){
	    	return ResponseEntity.ok(service.getByRouteIdAndStatus(routeId, status));
	    }
	    
	    @GetMapping("/by-date")
	    public ResponseEntity<Page<TripResponse>> getByDate(@RequestParam LocalDate date, Pageable pageable) {
	        var page = service.getByDate(date, pageable);
	        return ResponseEntity.ok(page);
	    }
}
