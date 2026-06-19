package domitila.repository;

import domitila.entity.Proyecto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    @Query("""
            SELECT DISTINCT p
            FROM Proyecto p
            LEFT JOIN FETCH p.tecnicos
            """)
    List<Proyecto> findAllConTecnicos();

    @Query("""
            SELECT DISTINCT p
            FROM Proyecto p
            LEFT JOIN FETCH p.tecnicos ts
            WHERE EXISTS (
                SELECT 1
                FROM p.tecnicos t
                WHERE LOWER(t.email) = LOWER(:email)
            )
            """)
    List<Proyecto> findAsignadosPorTecnicoEmail(@Param("email") String email);

    @Query("""
            SELECT DISTINCT p
            FROM Proyecto p
            LEFT JOIN FETCH p.tecnicos
            WHERE p.id = :id
            """)
    Optional<Proyecto> findByIdConTecnicos(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT p
            FROM Proyecto p
            LEFT JOIN FETCH p.tecnicos ts
            WHERE p.id = :id
              AND EXISTS (
                  SELECT 1
                  FROM p.tecnicos t
                  WHERE LOWER(t.email) = LOWER(:email)
              )
            """)
    Optional<Proyecto> findByIdAndTecnicoEmail(@Param("id") Long id, @Param("email") String email);
}
