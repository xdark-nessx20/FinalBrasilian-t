package co.edu.unimagdalena.finalbrasiliant.api.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class AssignmentDTOs {
    public record AssignmentCreateRequest(@NotNull Long tripId, @NotNull Long driverId, @NotNull Long dispatcherId, Boolean checkListOk) implements Serializable {}
    public record AssignmentUpdateRequest(@NotNull Boolean checkListOk, Long driverId, Long dispatcherId) implements Serializable {}
    public record AssignmentResponse(Long id, Long tripId, UserSummary driver, UserSummary dispatcher,
                                     Boolean checkListOk, OffsetDateTime assignedAt) implements Serializable {}

    public record UserSummary(Long id, String userName) implements Serializable {}
}
