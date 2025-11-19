package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ConfigDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.services.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/configs")
public class ConfigController {
    private final ConfigService service;

    @PostMapping
    public ResponseEntity<ConfigResponse> createConfig(@Valid @RequestBody ConfigCreateRequest request, UriComponentsBuilder uriBuilder) {
        var body = service.create(request);
        var location = uriBuilder.path("/api/v1/admin/configs/{key}").buildAndExpand(body.key()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{key}")
    public ResponseEntity<ConfigResponse> getConfig(@PathVariable String key) {
        return ResponseEntity.ok(service.get(key));
    }

    @PatchMapping("/{key}")
    public ResponseEntity<ConfigResponse> updateConfig(@PathVariable String key, @RequestBody ConfigUpdateRequest request){
        return ResponseEntity.ok(service.update(key, request));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        service.delete(key);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ConfigResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
