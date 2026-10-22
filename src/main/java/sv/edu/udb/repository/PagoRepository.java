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




    Page<Pago> findByCliente_NombreContainingIgnoreCase(
            String nombreCliente,
            Pageable paginacion
    );


    List<Pago> findByCotizacion_IdCotizacion(
            Integer idCotizacion
    );


    // Lo ya pagado de cada concepto (todos los estados). idPagoExcluido deja
    // fuera el pago que se está editando; null no excluye ninguno.
    @Query("""
          SELECT COALESCE(SUM(p.monto), 0)
          FROM Pago p
          WHERE p.cotizacion.idCotizacion = :idCotizacion
            AND (:idPagoExcluido IS NULL
                 OR p.idPago <> :idPagoExcluido)
          """)
    BigDecimal sumarPagadoCotizacion(
            @Param("idCotizacion")
            Integer idCotizacion,
            @Param("idPagoExcluido")
            Integer idPagoExcluido
    );


    @Query("""
          SELECT COALESCE(SUM(p.monto), 0)
          FROM Pago p
          WHERE p.idAlquiler = :idAlquiler
            AND (:idPagoExcluido IS NULL
                 OR p.idPago <> :idPagoExcluido)
          """)
    BigDecimal sumarPagadoAlquiler(
            @Param("idAlquiler")
            Integer idAlquiler,
            @Param("idPagoExcluido")
            Integer idPagoExcluido
    );


    @Query("""
          SELECT COALESCE(SUM(p.monto), 0)
          FROM Pago p
          WHERE p.idSolicitudCatering = :idSolicitud
            AND (:idPagoExcluido IS NULL
                 OR p.idPago <> :idPagoExcluido)
          """)
    BigDecimal sumarPagadoCatering(
            @Param("idSolicitud")
            Integer idSolicitud,
            @Param("idPagoExcluido")
            Integer idPagoExcluido
    );


    @Query("""
          SELECT COALESCE(SUM(p.monto), 0)
          FROM Pago p
          WHERE p.idInscripcion = :idInscripcion
            AND (:idPagoExcluido IS NULL
                 OR p.idPago <> :idPagoExcluido)
          """)
    BigDecimal sumarPagadoInscripcion(
            @Param("idInscripcion")
            Integer idInscripcion,
            @Param("idPagoExcluido")
            Integer idPagoExcluido
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

