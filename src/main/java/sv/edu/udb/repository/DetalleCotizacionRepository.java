package sv.edu.udb.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.DetalleCotizacion;


@Repository
public interface DetalleCotizacionRepository extends JpaRepository<DetalleCotizacion, Integer> {
    List<DetalleCotizacion> findByCotizacion_IdCotizacion(Integer idCotizacion);
    void deleteByCotizacion_IdCotizacion(Integer idCotizacion);
}
