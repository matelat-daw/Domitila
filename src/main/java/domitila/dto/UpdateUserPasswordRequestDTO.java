package domitila.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserPasswordRequestDTO (
    @NotBlank(message = "La nueva clave es obligatoria")
    @Size(min = 6, message = "La nueva clave debe tener al menos 6 caracteres")
    String nuevaClave
) {}
