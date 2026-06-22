package domitila.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRoleRequestDTO (
    @NotBlank(message = "El rol es obligatorio")
    String role,
    @NotBlank(message = "La acción del rol es obligatoria")
    String action
) {}
