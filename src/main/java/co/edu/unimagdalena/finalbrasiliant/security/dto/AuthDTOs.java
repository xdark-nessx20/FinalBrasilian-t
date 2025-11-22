package co.edu.unimagdalena.finalbrasiliant.security.dto;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDTOs {
    public record RegisterRequest(@NotBlank String userName, @NotBlank @Email String email, @NotNull Role role,
                                  @NotBlank @Size(min = 8) String password, @NotBlank String phone) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, String tokenType, long expirationInSeconds) {}
}
