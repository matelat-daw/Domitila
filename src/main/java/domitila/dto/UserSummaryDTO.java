package domitila.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String email;
    private String telefono;
    private Set<String> roles;
}