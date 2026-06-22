package domitila.auth.repository;

import domitila.auth.entity.TipoContratoCatalogo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoContratoCatalogoRepository extends JpaRepository<TipoContratoCatalogo, Integer> {

    List<TipoContratoCatalogo> findAllByOrderByNombreAsc();

    Optional<TipoContratoCatalogo> findByNombreIgnoreCase(String nombre);
}
