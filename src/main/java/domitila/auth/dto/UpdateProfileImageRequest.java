package domitila.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageRequest(
        @NotBlank(message = "La ruta de la imagen de perfil es obligatoria")
        String imagenPerfil
) {}
