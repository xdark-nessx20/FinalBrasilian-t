package co.edu.unimagdalena.finalbrasiliant.api.dto;

import java.io.Serializable;
import java.util.Set;

import jakarta.annotation.Nonnull;

public class BusDTO {
	public record busCreateRequest(@Nonnull String plate, @Nonnull Integer capacity, 
			@Nonnull Set<String> amenities) implements Serializable{};
			
	public record busUpdateRequest(String plate, Integer capacity, 
			Set<String> amenities, String status) implements Serializable{};
	
	public record busResponse(Long id, String plate, Integer capacity,
			Set<String> amenities, String status) implements Serializable{};
}
