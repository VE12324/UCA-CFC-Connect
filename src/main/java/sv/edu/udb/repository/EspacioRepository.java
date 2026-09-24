package sv.edu.udb.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.model.Espacio;

public interface EspacioRepository extends JpaRepository<Espacio, Integer> {

    Page<Espacio> findByNombreContainingIgnoreCaseOrUbicacionContainingIgnoreCase(
            String nombre,
            String ubicacion,
            Pageable paginacion
    );

    Optional<Espacio> findByNombreIgnoreCase(String nombre);

    long countByEstado(String estado);
}