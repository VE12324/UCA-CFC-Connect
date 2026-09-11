package sv.edu.udb.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Modalidad;

@Repository
public interface ModalidadRepository extends JpaRepository<Modalidad, Integer>{
    // Validar duplicados por nombre
    Optional<Modalidad> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);

    // Búsqueda para filtros y paginación
    Page<Modalidad> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
            String nombre,
            String descripcion,
            Pageable pageable
    );
}
