package domitila.personal;

import domitila.auth.enums.RoleName;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final GeneroCatalogoRepository generoCatalogoRepository;
    private final TipoJornadaCatalogoRepository tipoJornadaCatalogoRepository;
    private final TipoContratoCatalogoRepository tipoContratoCatalogoRepository;

    @GetMapping
    public ResponseEntity<CatalogsResponseDTO> obtenerCatalogos() {
        CatalogsResponseDTO response = new CatalogsResponseDTO(
                generoCatalogoRepository.findAllByOrderByNombreAsc().stream().map(this::toCatalogOption).toList(),
                tipoJornadaCatalogoRepository.findAllByOrderByNombreAsc().stream().map(this::toCatalogOption).toList(),
                tipoContratoCatalogoRepository.findAllByOrderByNombreAsc().stream().map(this::toCatalogOption).toList(),
                mapValues(GrupoProfesional.values(), GrupoProfesional::getCode),
                mapValues(ConvenioLaboral.values(), ConvenioLaboral::getDisplayName),
                mapValues(RoleName.values(), RoleName::name)
        );

        return ResponseEntity.ok(response);
    }

    private <T extends Enum<T>> List<String> mapValues(T[] values, Function<T, String> valueExtractor) {
        return Arrays.stream(values)
                .map(valueExtractor)
                .toList();
    }

    private CatalogOptionDTO toCatalogOption(GeneroCatalogo genero) {
        return new CatalogOptionDTO(genero.getId(), genero.getNombre());
    }

    private CatalogOptionDTO toCatalogOption(TipoJornadaCatalogo tipoJornada) {
        return new CatalogOptionDTO(tipoJornada.getId(), tipoJornada.getNombre());
    }

    private CatalogOptionDTO toCatalogOption(TipoContratoCatalogo tipoContrato) {
        return new CatalogOptionDTO(tipoContrato.getId(), tipoContrato.getNombre());
    }
}