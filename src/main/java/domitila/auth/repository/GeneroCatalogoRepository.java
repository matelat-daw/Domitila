package domitila.auth.repository;

import domitila.auth.entity.GeneroCatalogo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneroCatalogoRepository extends JpaRepository<GeneroCatalogo, Integer> {

    List<GeneroCatalogo> findAllByOrderByNombreAsc();

    Optional<GeneroCatalogo> findByNombreIgnoreCase(String nombre);
}
