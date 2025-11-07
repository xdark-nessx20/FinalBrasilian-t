package co.edu.unimagdalena.finalbrasiliant.api.dto;

import jakarta.annotation.Nonnull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;

public class TripDTO {

    public record TripCreateRequest(
        @Nonnull Long routeId,
        @Nonnull Long busId,
        @Nonnull LocalDate date,
        @Nonnull OffsetDateTime departureAt,
        @Nonnull OffsetDateTime arrivalETA
    ) implements Serializable {}

    public record TripUpdateRequest(
        Long routeId,
        Long busId,
        LocalDate date,
        OffsetDateTime departureAt,
        OffsetDateTime arrivalETA,
        TripStatus status
    ) implements Serializable {}

    public record TripResponse(
        Long id,
        Long routeId,
        Long busId,
        LocalDate date,
        OffsetDateTime departureAt,
        OffsetDateTime arrivalETA,
        TripStatus status
    ) implements Serializable {}
}
