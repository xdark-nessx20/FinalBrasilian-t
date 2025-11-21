package co.edu.unimagdalena.finalbrasiliant.api.dto;

import java.io.Serializable;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SeatDTO {
	public record SeatCreateRequest(
			@NotNull Long bus_id,
			@NotBlank String number,
			@NotNull SeatType type
	    ) implements Serializable {}

	    public record SeatUpdateRequest(
	        String number,
	        SeatType type
	    ) implements Serializable {}

	    public record SeatResponse(
	        Long id,
	        Long bus_id,
	        String number,
	        SeatType type
	    ) implements Serializable {}
}
