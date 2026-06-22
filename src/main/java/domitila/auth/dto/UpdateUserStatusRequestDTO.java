package domitila.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequestDTO(
        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo
) {}
