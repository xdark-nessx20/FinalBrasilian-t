package co.edu.unimagdalena.finalbrasiliant.api.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;

public class TripDTO {

    public record TripCreateRequest(
        @NotNull Long bus_id,
        @NotNull LocalDate date,
        @NotNull OffsetDateTime departureAt,
        @NotNull OffsetDateTime arrivalETA
    ) implements Serializable {}

    public record TripUpdateRequest(
        Long route_id,
        Long bus_id,
        LocalDate date,
        OffsetDateTime departureAt,
        OffsetDateTime arrivalETA,
        TripStatus status
    ) implements Serializable {}

    public record TripResponse(
        Long id,
        Long route_id,
        Long bus_id,
        LocalDate date,
        OffsetDateTime departureAt,
        OffsetDateTime arrivalETA,
        TripStatus status
    ) implements Serializable {}
}
