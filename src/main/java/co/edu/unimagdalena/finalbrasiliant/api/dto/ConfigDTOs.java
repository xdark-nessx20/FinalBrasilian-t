package co.edu.unimagdalena.finalbrasiliant.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public class ConfigDTOs {
    public record ConfigCreateRequest(@NotBlank String key, @NotNull BigDecimal value) implements Serializable {}
    public record ConfigUpdateRequest(@NotNull BigDecimal value) implements Serializable {}
    public record ConfigResponse(String key, BigDecimal value) implements Serializable {}
}
