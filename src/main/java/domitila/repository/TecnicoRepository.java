package domitila.repository;

import domitila.entity.Tecnico;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    Optional<Tecnico> findByCorreoElectronico(String correoElectronico);
    boolean existsByCorreoElectronico(String correoElectronico);
    boolean existsByTelefono(String telefono);

    @Query("""
            SELECT t
            FROM Tecnico t
            WHERE LOWER(t.correoElectronico) <> LOWER(:correoElectronicoLogueado)
              AND (:nombre IS NULL OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:apellido1 IS NULL OR LOWER(t.apellido1) LIKE LOWER(CONCAT('%', :apellido1, '%')))
            """)
    Page<Tecnico> buscarUsuarios(
            @Param("correoElectronicoLogueado") String correoElectronicoLogueado,
            @Param("nombre") String nombre,
            @Param("apellido1") String apellido1,
            Pageable pageable
    );
}
