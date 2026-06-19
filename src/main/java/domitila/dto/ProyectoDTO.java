package domitila.dto;

import java.util.List;

public record ProyectoDTO(
    Long id,
    String nombre,
    String descripcion,
    String fechaInicio,
    List<ProyectoTecnicoDTO> tecnicos
) {}
