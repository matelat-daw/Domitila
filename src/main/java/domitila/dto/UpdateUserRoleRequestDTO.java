package domitila.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequestDTO (
    @NotNull(message = "El roleId es obligatorio")
    Long roleId
) {}
