package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.FareRuleDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;
import co.edu.unimagdalena.finalbrasiliant.services.FareRuleService;
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
@RequestMapping("/api/v1/fare-rules")
public class FareRuleController {

    private final FareRuleService service;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<FareRuleResponse> createFareRule(@Valid @RequestBody FareRuleCreateRequest request,
                                                           UriComponentsBuilder uriBuilder) {
        var body = service.create(request);
        var location = uriBuilder.path("/api/v1/fare-rules/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FareRuleResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<FareRuleResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody FareRuleUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-route-and-stops")
    public ResponseEntity<FareRuleResponse> getByRouteAndStops(@RequestParam Long routeId,
                                                               @RequestParam Long fromStopId,
                                                               @RequestParam Long toStopId) {
        return ResponseEntity.ok(service.getByRouteAndStops(routeId, fromStopId, toStopId));
    }

    @GetMapping("/by-route")
    public ResponseEntity<List<FareRuleResponse>> listByRoute(@RequestParam Long routeId) {
        return ResponseEntity.ok(service.listByRoute(routeId));
    }

    @GetMapping("/by-from-stop")
    public ResponseEntity<List<FareRuleResponse>> listByFromStop(@RequestParam Long fromStopId) {
        return ResponseEntity.ok(service.listByFromStop(fromStopId));
    }

    @GetMapping("/by-to-stop")
    public ResponseEntity<List<FareRuleResponse>> listByToStop(@RequestParam Long toStopId) {
        return ResponseEntity.ok(service.listByToStop(toStopId));
    }

    @GetMapping("/by-dynamic-pricing")
    public ResponseEntity<List<FareRuleResponse>> listByDynamicPricing(@RequestParam DynamicPricing dynamicPricing) {
        return ResponseEntity.ok(service.listByDynamicPricing(dynamicPricing));
    }

    @GetMapping("/by-route-and-pricing")
    public ResponseEntity<List<FareRuleResponse>> listByRouteAndDynamicPricing(@RequestParam Long routeId,
                                                                               @RequestParam DynamicPricing dynamicPricing) {
        return ResponseEntity.ok(service.listByRouteAndDynamicPricing(routeId, dynamicPricing));
    }
}
