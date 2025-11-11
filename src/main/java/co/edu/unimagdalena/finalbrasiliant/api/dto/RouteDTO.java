package co.edu.unimagdalena.finalbrasiliant.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RouteDTO {
	public record RouteCreateRequest(@NotBlank String code, @NotBlank String routeName, 
			@NotBlank String origin, @NotBlank String destination, 
			@NotNull BigDecimal distanceKM, @NotNull Integer durationMin) implements Serializable{};
	
	public record RouteUpdateRequest(String code, String routeName, 
			 String origin, String destination, BigDecimal distanceKM, Integer durationMin) implements Serializable{};
	
	public record RouteResponse(Long id, String code, String routeName, 
			 String origin, String destination, BigDecimal distanceKM, Integer durationMin) implements Serializable{};
}
