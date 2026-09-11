package sv.edu.udb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.ServicioCatering;

@Repository
public interface CateringRepository extends JpaRepository<ServicioCatering, Integer> {
    Page<ServicioCatering> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
            String nombre,
            String descripcion,
            Pageable pageable
    );
}