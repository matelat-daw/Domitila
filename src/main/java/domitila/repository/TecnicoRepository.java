package domitila.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import domitila.entity.Tecnico;
import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    Optional<Tecnico> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
}