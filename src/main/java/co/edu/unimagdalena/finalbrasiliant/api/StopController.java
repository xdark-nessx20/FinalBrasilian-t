package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.StopDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.services.StopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1")
public class StopController {

    private final StopService service;

    @PostMapping("/routes/{routeId}/stops")
    public ResponseEntity<StopResponse> createStop(@PathVariable Long routeId,
                                                   @Valid @RequestBody StopCreateRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        var body = service.create(request);
        var location = uriBuilder.path("/api/v1/stops/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/routes/{routeId}/stops")
    public ResponseEntity<List<StopResponse>> listByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.listByRoute(routeId));
    }

    @GetMapping("/stops/{id}")
    public ResponseEntity<StopResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/stops/{id}")
    public ResponseEntity<StopResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody StopUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/stops/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stops/by-name")
    public ResponseEntity<List<StopResponse>> listByName(@RequestParam String name) {
        return ResponseEntity.ok(service.listByName(name));
    }

    @GetMapping("/routes/{routeId}/stops/first")
    public ResponseEntity<StopResponse> getFirstStop(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.getFirstStop(routeId));
    }

    @GetMapping("/routes/{routeId}/stops/last")
    public ResponseEntity<StopResponse> getLastStop(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.getLastStop(routeId));
    }

    @GetMapping("/routes/{routeId}/stops/by-order")
    public ResponseEntity<StopResponse> getByRouteAndOrder(@PathVariable Long routeId,
                                                           @RequestParam Integer stopOrder) {
        return ResponseEntity.ok(service.getByRouteAndOrder(routeId, stopOrder));
    }

    @GetMapping("/routes/{routeId}/stops/by-order-range")
    public ResponseEntity<List<StopResponse>> listByRouteAndOrderRange(@PathVariable Long routeId,
                                                                       @RequestParam Integer startOrder,
                                                                       @RequestParam Integer endOrder) {
        return ResponseEntity.ok(service.listByRouteAndOrderRange(routeId, startOrder, endOrder));
    }

    /*@GetMapping("/routes/{routeId}/stops/count")
    public ResponseEntity<Long> countByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.countByRoute(routeId));
    }

    @GetMapping("/routes/{routeId}/stops/exists")
    public ResponseEntity<Boolean> existsByRouteAndName(@PathVariable Long routeId,
                                                        @RequestParam String name) {
        return ResponseEntity.ok(service.existsByRouteAndName(routeId, name));
    }*/
}
