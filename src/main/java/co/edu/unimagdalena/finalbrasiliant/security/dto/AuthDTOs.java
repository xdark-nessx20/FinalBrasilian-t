package co.edu.unimagdalena.finalbrasiliant.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDTOs {
    public record RegisterRequest(@NotBlank String userName, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 8) String password, @NotBlank String phone) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, String email, String role) {}
}
