package sv.edu.udb.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.EstadoCotizacion;


@Repository
public interface EstadoCotizacionRepository extends JpaRepository<EstadoCotizacion, Integer> {
    Optional<EstadoCotizacion> findByNombreIgnoreCase(String nombre);
}
