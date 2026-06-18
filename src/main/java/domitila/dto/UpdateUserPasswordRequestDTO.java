package domitila.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPasswordRequestDTO {
    @NotBlank(message = "La nueva clave es obligatoria")
    @Size(min = 6, message = "La nueva clave debe tener al menos 6 caracteres")
    private String nuevaClave;
}