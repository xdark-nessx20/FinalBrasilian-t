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
@RequestMapping("/api/v1")
public class TripController {
	private final TripService service;
	
	@PostMapping("/routes/{routeId}/trips")
	public ResponseEntity<TripResponse> createTrip(
	        @PathVariable Long routeId,
	        @Valid @RequestBody TripCreateRequest request,
	        UriComponentsBuilder uriBuilder) {
	    var body = service.create(routeId, request);
	    var location = uriBuilder.path("/api/v1/routes/{routeId}/trips/{id}")
	                             .buildAndExpand(routeId, body.id())
	                             .toUri();
	    return ResponseEntity.created(location).body(body);
	}


	 @GetMapping("/trips/{tripId}")
	 public ResponseEntity<TripResponse> get(@PathVariable Long tripId) {
		 return ResponseEntity.ok(service.get(tripId));
	    }

	 @PatchMapping("/trips/{tripId}")
	 public ResponseEntity<TripResponse> update(
	            @PathVariable Long routeId,
	            @PathVariable Long tripId,
	            @Valid @RequestBody TripUpdateRequest request) {
		 return ResponseEntity.ok(service.update(tripId, request));
	    }

	 @DeleteMapping("/trips/{tripId}")
	 public ResponseEntity<Void> delete(@PathVariable Long tripId) {
		 service.delete(tripId);
	     return ResponseEntity.noContent().build();
	    }
	
	 @GetMapping("/routes/{routeId}/trips")
	 public ResponseEntity<Page<TripResponse>> getByRouteId(@PathVariable Long routeId, Pageable pageable) {
	     var page = service.getAllByRouteId(routeId, pageable);
	     return ResponseEntity.ok(page);
	    }
	    
	 @GetMapping("/trips/by-bus")
	 public ResponseEntity<Page<TripResponse>> getByBusId(@RequestParam Long busId, Pageable pageable) {
		 var page = service.getAllByBusId(busId, pageable);
	     return ResponseEntity.ok(page);
	    }
	    
	 @GetMapping("/trips/by-departure")
	 public ResponseEntity<Page<TripResponse>> getByDepartureBetween(@RequestParam OffsetDateTime start, 
			@RequestParam OffsetDateTime end,
			Pageable pageable) {
	     var page = service.getByDepartureBetween(start, end, pageable);
	     return ResponseEntity.ok(page);
	    }

	 @GetMapping("/trips/by-arrival")
	 public ResponseEntity<Page<TripResponse>> getByArrivalBetween(@RequestParam OffsetDateTime start,
			 @RequestParam OffsetDateTime end, 
			 Pageable pageable) {
		 var page = service.getByArrivalBetween(start, end, pageable);
	     return ResponseEntity.ok(page);
	    }

	 @GetMapping("/trips/by-status")
	 public ResponseEntity<Page<TripResponse>> getByStatus(@RequestParam TripStatus status, Pageable pageable) {
	     var page = service.getByStatus(status, pageable);
	     return ResponseEntity.ok(page);
	    }

	 @GetMapping("/trips/search")
	 public ResponseEntity<List<TripResponse>> getByRouteIdAndStatus(@RequestParam Long routeId, 
			 @RequestParam TripStatus status) {
	     return ResponseEntity.ok(service.getByRouteIdAndStatus(routeId, status));
	    }

	 @GetMapping("/trips/by-date")
	 public ResponseEntity<Page<TripResponse>> getByDate(@RequestParam LocalDate date, Pageable pageable) {
	     var page = service.getByDate(date, pageable);
	     return ResponseEntity.ok(page);
	    }
	 
	 @GetMapping("/trips/search")
	 public ResponseEntity<List<TripResponse>> getByRouteIdAndDate(@RequestParam Long routeId, 
			 @RequestParam LocalDate date) {
	     return ResponseEntity.ok(service.getByRouteIdAndDate(routeId, date));
	    }
}
