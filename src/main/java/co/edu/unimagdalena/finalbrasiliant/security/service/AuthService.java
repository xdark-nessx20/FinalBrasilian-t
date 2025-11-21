package co.edu.unimagdalena.finalbrasiliant.security.service;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.security.dto.AuthDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;
    private final UserRepository users;

    public AuthResponse register(RegisterRequest request){
        if (users.existsByEmailIgnoreCase(request.email()))
            throw new AlreadyExistsException("Email already in use.");

        var user = User.builder().email(request.email()).passwordHash(encoder.encode(request.password()))
                .phone(request.phone()).userName(request.userName()).role(request.role()).build();
        users.save(user);
        var role = user.getRole().name();

        var principal = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash()).authorities(role).build();

        var token = jwt.generateToken(principal, Map.of("expirationInSeconds", role));
        return new AuthResponse(token, "Bearer", jwt.getExpirationSeconds());
    }

    public AuthResponse login(LoginRequest request){
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var user = users.findByEmailIgnoreCase(request.email()).orElseThrow(
                () -> new NotFoundException("User not found.")
        );
        var principal = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(user.getRole().name()).build();
        var token = jwt.generateToken(principal, Map.of("expirationInSeconds", user.getRole().name()));
        return new AuthResponse(token, "Bearer", jwt.getExpirationSeconds());
    }
}
