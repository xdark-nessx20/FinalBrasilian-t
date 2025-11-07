package co.edu.unimagdalena.finalbrasiliant.api.dto;

import java.io.Serializable;

import jakarta.annotation.Nonnull;

public class UserDTO {
	public record userCreateRequest(@Nonnull String userName, @Nonnull String email, 
			@Nonnull String phone, @Nonnull String role, @Nonnull String passwordHash) implements Serializable{};
			
	public record userUpdateRequest(String userName, String email, 
			String phone, String role, 
			Boolean status, String passwordHash) implements Serializable{};
	
	public record userResponse(String userName, String email, 
			String phone, String role, 
			Boolean status, String passwordHash) implements Serializable{};
}
