package co.edu.unimagdalena.finalbrasiliant.api;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request,
            UriComponentsBuilder uriBuilder) {
        var body = service.create(request);
        var location = uriBuilder.path("/api/v1/users/{id}")
                                 .buildAndExpand(body.id())
                                 .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PASSENGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK')")
    @GetMapping("/by-name")
    public ResponseEntity<UserResponse> getByUserName(@RequestParam String userName) {
        return ResponseEntity.ok(service.getByUserName(userName));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK')")
    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK')")
    @GetMapping("/by-phone")
    public ResponseEntity<UserResponse> getByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(service.getByPhone(phone));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/by-created_at")
    public ResponseEntity<Page<UserResponse>> getByCreatedAtBetween(@RequestParam OffsetDateTime start, 
    		@RequestParam OffsetDateTime end, Pageable pageable) {
        var page = service.getByCreatedAtBetween(start, end, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLERK')")
    @GetMapping("/by-role")
    public ResponseEntity<List<UserResponse>> getByRole(@RequestParam Role role){
        return ResponseEntity.ok(service.getByRole(role));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/by-status")
    public ResponseEntity<Page<UserResponse>> getByStatus( @RequestParam Boolean status,Pageable pageable) {
        var page = service.getByStatus(status, pageable);
        return ResponseEntity.ok(page);
    }
}
