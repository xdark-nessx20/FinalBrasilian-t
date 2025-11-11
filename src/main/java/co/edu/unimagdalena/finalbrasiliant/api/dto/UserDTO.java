package co.edu.unimagdalena.finalbrasiliant.api.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserDTO {
	public record userCreateRequest(@NotBlank String userName, @NotBlank String email, 
			@NotBlank String phone, @NotNull Role role, @NotBlank String passwordHash) implements Serializable{};
			
	public record userUpdateRequest(String userName, String email, 
			String phone, Role role, 
			Boolean status, String passwordHash) implements Serializable{};
	
	public record userResponse(Long id, String userName, String email, 
			String phone, Role role, 
			Boolean status, OffsetDateTime createdAt) implements Serializable{};
}
