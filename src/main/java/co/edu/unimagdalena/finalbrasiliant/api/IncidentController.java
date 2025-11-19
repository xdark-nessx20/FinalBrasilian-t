package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.IncidentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.EntityType;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.IncidentType;
import co.edu.unimagdalena.finalbrasiliant.services.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/incidents")
public class IncidentController {
    private final IncidentService service;

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody IncidentCreateRequest request, UriComponentsBuilder uriBuilder) {
        var body = service.create(request);
        var location = uriBuilder.path("/api/v1/incidents/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IncidentResponse> update(@PathVariable Long id, @Valid @RequestBody IncidentUpdateRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<IncidentResponse>> list(Pageable pageable){
        var page = service.getAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/by-date")
    public ResponseEntity<Page<IncidentResponse>> listByDate(@RequestParam("from") OffsetDateTime from, @RequestParam("to") OffsetDateTime to,
                                                            Pageable pageable){
        var page = service.listByCreatedAt(from, to, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/by-typeAndEntity")
    public ResponseEntity<List<IncidentResponse>> listByTypeAndEType(@RequestParam(value = "type", required = false) IncidentType type,
                                                                     @RequestParam(value = "entity", required = false) EntityType entityType){
        return ResponseEntity.ok(service.listByTypeAndEntityType(type, entityType));
    }
}
