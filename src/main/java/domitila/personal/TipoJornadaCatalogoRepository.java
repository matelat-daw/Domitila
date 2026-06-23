package domitila.personal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoJornadaCatalogoRepository extends JpaRepository<TipoJornadaCatalogo, Integer> {

    List<TipoJornadaCatalogo> findAllByOrderByNombreAsc();

    Optional<TipoJornadaCatalogo> findByNombreIgnoreCase(String nombre);
}