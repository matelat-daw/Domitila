package domitila.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO (

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre must tener entre 2 y 100 caracteres")
    String nombre,

    @NotBlank(message = "El primer apellido no puede estar vacío")
    @Size(max = 24, message = "El primer apellido no puede tener más de 24 caracteres")
    String apellido1,

    @Size(max = 24, message = "El segundo apellido no puede tener más de 24 caracteres")
    String apellido2,

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    String email,

    @NotBlank(message = "La clave no puede estar vacía")
    @Size(min = 6, message = "La clave debe tener al menos 6 caracteres")
    String clave,

    @NotBlank(message = "El teléfono no puede estar vacío")
    String telefono // Opcional, por eso no lleva @NotBlank
) {}