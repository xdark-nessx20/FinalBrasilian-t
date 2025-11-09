package co.edu.unimagdalena.finalbrasiliant.api.dto;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Set;

public class BusDTOs {

    public record BusCreateRequest(@NotBlank @Size(min = 6,max = 10) String plate,
                                   @Size(min = 10) Integer capacity, Set<String> amenities,
                                   @NotNull BusStatus status) implements Serializable {}

    public record BusUpdateRequest(@NotBlank @Size(min = 6,max = 10) String plate,
                                   @Size(min = 10) Integer capacity , Set<String> amenities,
                                   BusStatus status) implements Serializable {}

    public record BusResponse(Long id, String plate, Integer capacity, Set<String> amenities,
                              BusStatus status) implements Serializable {}
}
