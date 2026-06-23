package domitila.personal;

import java.util.List;

public record CatalogsResponseDTO(
        List<CatalogOptionDTO> generos,
        List<CatalogOptionDTO> tiposJornada,
        List<CatalogOptionDTO> tiposContrato,
        List<String> gruposProfesionales,
        List<String> conveniosLaborales,
        List<String> roles
) {}
