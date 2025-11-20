package co.edu.unimagdalena.finalbrasiliant.security.web;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.security.dto.AuthDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;
    private final UserRepository users;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        if (users.existsByEmailIgnoreCase(request.email())) return ResponseEntity.badRequest().build();

        var user = User.builder().email(request.email()).passwordHash(encoder.encode(request.password()))
                .phone(request.phone()).userName(request.userName()).role(request.role()).build();
        users.save(user);
        var role = user.getRole().name();

        var principal = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
            .password(user.getPasswordHash()).authorities(role).build();

        var token = jwt.generateToken(principal, Map.of("role", role));
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", jwt.getExpirationSeconds()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var user = users.findByEmailIgnoreCase(request.email()).orElseThrow();
        var principal = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(user.getRole().name()).build();
        var token = jwt.generateToken(principal, Map.of("role", user.getRole().name()));
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", jwt.getExpirationSeconds()));
    }
}
