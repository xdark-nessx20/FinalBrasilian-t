package co.edu.unimagdalena.finalbrasiliant.security.web;

import co.edu.unimagdalena.finalbrasiliant.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;
}
