package domitila.auth.controller;

import domitila.auth.dto.CatalogsResponseDTO;
import domitila.auth.enums.ConvenioLaboral;
import domitila.auth.enums.GrupoProfesional;
import domitila.auth.enums.RoleName;
import domitila.auth.enums.Genero;
import domitila.auth.enums.TipoContrato;
import domitila.auth.enums.TipoJornada;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogs")
public class CatalogController {

    @GetMapping
    public ResponseEntity<CatalogsResponseDTO> obtenerCatalogos() {
        CatalogsResponseDTO response = new CatalogsResponseDTO(
                mapValues(Genero.values(), Genero::getDisplayName),
                mapValues(TipoJornada.values(), TipoJornada::getDisplayName),
                mapValues(TipoContrato.values(), TipoContrato::getDisplayName),
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
}
