package domitila.dto;

import java.util.Set;

public record UserSummaryDTO (
    Integer id,
    String nombre,
    String apellido1,
    String apellido2,
    String correoElectronico,
    String telefono,
    Set<String> roles,
    String imagenPerfil,
    Boolean activo
) {}
