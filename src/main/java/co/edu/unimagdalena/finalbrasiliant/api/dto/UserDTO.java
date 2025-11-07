package co.edu.unimagdalena.finalbrasiliant.api.dto;

import java.io.Serializable;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import jakarta.validation.constraints.NotNull;

public class UserDTO {
	public record userCreateRequest(@NotNull String userName, @NotNull String email, 
			@NotNull String phone, @NotNull Role role, @NotNull String passwordHash) implements Serializable{};
			
	public record userUpdateRequest(String userName, String email, 
			String phone, Role role, 
			Boolean status, String passwordHash) implements Serializable{};
	
	public record userResponse(Long id, String userName, String email, 
			String phone, Role role, 
			Boolean status, String passwordHash) implements Serializable{};
}
