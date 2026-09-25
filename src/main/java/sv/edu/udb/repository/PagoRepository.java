package sv.edu.udb.repository;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Pago;


@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {


     Page<Pago>
            findByCliente_NombreContainingIgnoreCaseOrNumeroReferenciaExternaContainingIgnoreCase(
                    String nombreCliente,
                    String numeroReferenciaExterna,
                    Pageable paginacion
            );

    List<Pago> findByCotizacion_IdCotizacion(
            Integer idCotizacion
    );

    Page<Pago> findByCliente_Usuario_EmailIgnoreCase(
            String correo,
            Pageable paginacion
    );

    Optional<Pago>
            findByIdPagoAndCliente_Usuario_EmailIgnoreCase(
                    Integer idPago,
                    String correo
            );

    long countByEstadoPago_NombreIgnoreCase(
            String nombreEstado
    );

    @Query("""
           SELECT COALESCE(SUM(p.monto), 0)
           FROM Pago p
           WHERE LOWER(p.estadoPago.nombre)
                 = LOWER(:nombreEstado)
           """)
    BigDecimal sumarMontoPorEstado(
            @Param("nombreEstado")
            String nombreEstado
    );

    @Query("""
           SELECT COALESCE(SUM(p.monto), 0)
           FROM Pago p
           """)
    BigDecimal sumarMontoTotal();

    @Query("""
           SELECT p.metodoPago.nombre,
                  COALESCE(SUM(p.monto), 0)
           FROM Pago p
           GROUP BY p.metodoPago.nombre
           """)
    List<Object[]> sumarMontoPorMetodo();

    @Query("""
           SELECT p.estadoPago.nombre,
                  COALESCE(SUM(p.monto), 0)
           FROM Pago p
           GROUP BY p.estadoPago.nombre
           """)
    List<Object[]> sumarMontoPorEstadoAgrupado();

    long countByEstadoPago_NombreIgnoreCaseAndFechaBetween(
            String nombreEstado,
            LocalDate desde,
            LocalDate hasta
    );
}
