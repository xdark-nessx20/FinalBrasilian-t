package co.edu.unimagdalena.finalbrasiliant.api.dto;

import java.io.Serializable;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import jakarta.annotation.Nonnull;

public class SeatDTO {
	public record SeatCreateRequest(
	        @Nonnull Long busId,
	        @Nonnull String number,
	        @Nonnull SeatType type
	    ) implements Serializable {}

	    public record SeatUpdateRequest(
	        Long busId,
	        String number,
	        SeatType type
	    ) implements Serializable {}

	    public record SeatResponse(
	        Long id,
	        Long busId,
	        String number,
	        SeatType type
	    ) implements Serializable {}
}
