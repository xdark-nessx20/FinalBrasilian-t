package co.edu.unimagdalena.finalbrasiliant.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

public class RouteDTO {
	public record RouteCreateRequest(@NotNull String code, @NotNull String routeName, 
			@NotNull String origin, @NotNull String destination, 
			@NotNull BigDecimal distanceKM, @NotNull Integer durationMin) implements Serializable{};
	
	public record RouteUpdateRequest(String code, String routeName, 
			 String origin, String destination, BigDecimal distanceKM, Integer durationMin) implements Serializable{};
	
	public record RouteResponse(Long id, String code, String routeName, 
			 String origin, String destination, BigDecimal distanceKM, Integer durationMin) implements Serializable{};
}
