package domitila.repository;

import domitila.entity.Personal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonalRepository extends JpaRepository<Personal, Integer> {
    Optional<Personal> findByCorreoElectronico(String correoElectronico);
    boolean existsByDni(String dni);
    boolean existsByCorreoElectronico(String correoElectronico);
    boolean existsByTelefono(String telefono);

    @Query("""
            SELECT p
            FROM Personal p
            WHERE LOWER(p.correoElectronico) <> LOWER(:correoElectronicoLogueado)
              AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:apellido1 IS NULL OR LOWER(p.apellido1) LIKE LOWER(CONCAT('%', :apellido1, '%')))
            """)
    Page<Personal> buscarPersonal(
            @Param("correoElectronicoLogueado") String correoElectronicoLogueado,
            @Param("nombre") String nombre,
            @Param("apellido1") String apellido1,
            Pageable pageable
    );
}
