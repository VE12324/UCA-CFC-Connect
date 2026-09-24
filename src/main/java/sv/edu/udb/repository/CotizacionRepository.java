package sv.edu.udb.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Cotizacion;


@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Integer> {


    Page<Cotizacion> findByEstadoCotizacion_NombreIgnoreCaseIn(List<String> nombresEstado, Pageable paginacion);


    long countByEstadoCotizacion_NombreIgnoreCaseIn(List<String> nombresEstado);
}

