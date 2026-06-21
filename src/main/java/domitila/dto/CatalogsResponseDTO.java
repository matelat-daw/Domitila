package domitila.dto;

import java.util.List;

public record CatalogsResponseDTO(
        List<String> sexos,
        List<String> tiposJornada,
        List<String> tiposContrato,
        List<String> gruposProfesionales,
        List<String> conveniosLaborales,
        List<String> roles
) {}
