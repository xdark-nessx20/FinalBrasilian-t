package co.edu.unimagdalena.finalbrasiliant.api.dto;

import java.io.Serializable;


import jakarta.annotation.Nonnull;

public class RouteDTO {
	public record RouteCreateRequest(@Nonnull String code, @Nonnull String routeName, 
			@Nonnull String Origin, @Nonnull String destination) implements Serializable{};
	
	public record RouteUpdateRequest( String code, String routeName, 
			 String Origin, String destination) implements Serializable{};
	
	public record RouteResponse(Long id, String code, String routeName, 
			 String Origin, String destination) implements Serializable{};
}
