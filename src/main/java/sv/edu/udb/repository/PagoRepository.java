package sv.edu.udb.repository;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Pago;


@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {


    Page<Pago> findByCliente_NombreContainingIgnoreCaseOrNumeroReferenciaExternaContainingIgnoreCase(
            String nombreCliente, String numeroReferenciaExterna, Pageable paginacion
    );


    List<Pago> findByCotizacion_IdCotizacion(Integer idCotizacion);


    long countByEstadoPago_NombreIgnoreCase(String nombreEstado);


    @Query("select coalesce(sum(p.monto), 0) from Pago p where lower(p.estadoPago.nombre) = lower(:nombreEstado)")
    BigDecimal sumarMontoPorEstado(@Param("nombreEstado") String nombreEstado);


    @Query("select coalesce(sum(p.monto), 0) from Pago p")
    BigDecimal sumarMontoTotal();


    @Query("select p.metodoPago.nombre, coalesce(sum(p.monto), 0) from Pago p group by p.metodoPago.nombre")
    List<Object[]> sumarMontoPorMetodo();


    @Query("select p.estadoPago.nombre, coalesce(sum(p.monto), 0) from Pago p group by p.estadoPago.nombre")
    List<Object[]> sumarMontoPorEstadoAgrupado();


    long countByEstadoPago_NombreIgnoreCaseAndFechaBetween(String nombreEstado, LocalDate desde, LocalDate hasta);
}
