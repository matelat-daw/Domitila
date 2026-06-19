package domitila.dto;

import java.util.Set;

public record UserSummaryDTO (
    Long id,
    String nombre,
    String apellido1,
    String apellido2,
    String email,
    String telefono,
    Set<String> roles
) {}
