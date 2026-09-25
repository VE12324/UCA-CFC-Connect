package sv.edu.udb.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.CotizacionRequestDTO;
import sv.edu.udb.dto.CotizacionRespuestaDTO;
import sv.edu.udb.dto.DetalleCotizacionItemDTO;
import sv.edu.udb.dto.DetalleCotizacionRespuestaDTO;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Cotizacion;
import sv.edu.udb.model.DetalleCotizacion;
import sv.edu.udb.model.EstadoCotizacion;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.CotizacionRepository;
import sv.edu.udb.repository.DetalleCotizacionRepository;
import sv.edu.udb.repository.EstadoCotizacionRepository;


@Service
@RequiredArgsConstructor
public class CotizacionService {
    
 private static final BigDecimal FACTOR_IVA =
            new BigDecimal("1.13");

    private static final String ESTADO_PENDIENTE =
            "Pendiente";

    private static final String ESTADO_EN_PROCESO =
            "En proceso";

    private static final String ESTADO_APROBADA =
            "Aprobada";

    private static final String ESTADO_RECHAZADA =
            "Rechazada";

    private final CotizacionRepository cotizacionRepositorio;

    private final DetalleCotizacionRepository
            detalleCotizacionRepositorio;

    private final EstadoCotizacionRepository
            estadoCotizacionRepositorio;

    private final ClienteRepository clienteRepositorio;

    @Transactional(readOnly = true)
    public Page<CotizacionRespuestaDTO>
            listarPendientesOEnProceso(
                    Pageable paginacion) {

        return cotizacionRepositorio
                .findByEstadoCotizacion_NombreIgnoreCaseIn(
                        List.of(
                                ESTADO_PENDIENTE,
                                ESTADO_EN_PROCESO
                        ),
                        paginacion
                )
                .map(this::mapearRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<CotizacionRespuestaDTO> listarTodas(
            Pageable paginacion) {

        return cotizacionRepositorio
                .findAll(paginacion)
                .map(this::mapearRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<CotizacionRespuestaDTO> listarCotizaciones(
            String busqueda,
            Pageable paginacion) {

        if (busqueda == null || busqueda.isBlank()) {

            return cotizacionRepositorio
                    .findAll(paginacion)
                    .map(this::mapearRespuesta);
        }

        String texto = busqueda.trim();

        return cotizacionRepositorio
                .findByCliente_NombreContainingIgnoreCaseOrEstadoCotizacion_NombreContainingIgnoreCase(
                        texto,
                        texto,
                        paginacion
                )
                .map(this::mapearRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<CotizacionRespuestaDTO>
            listarPorCorreoCliente(
                    String correo,
                    Pageable paginacion) {

        if (correo == null || correo.isBlank()) {

            throw new IllegalArgumentException(
                    "No se pudo identificar la cuenta del cliente."
            );
        }

        return cotizacionRepositorio
                .findByCliente_Usuario_EmailIgnoreCase(
                        correo.trim(),
                        paginacion
                )
                .map(this::mapearRespuesta);
    }

    @Transactional(readOnly = true)
    public CotizacionRespuestaDTO
            buscarPorIdDelCliente(
                    Integer id,
                    String correo) {

        Cotizacion cotizacion = cotizacionRepositorio
                .findByIdCotizacionAndCliente_Usuario_EmailIgnoreCase(
                        id,
                        correo
                )
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró la cotización solicitada."
                ));

        return mapearRespuesta(cotizacion);
    }

    @Transactional(readOnly = true)
    public CotizacionRespuestaDTO buscarPorId(
            Integer id) {

        return mapearRespuesta(
                buscarEntidadPorId(id)
        );
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {

        return clienteRepositorio.findAll(
                Sort.by("nombre").ascending()
        );
    }

    @Transactional(readOnly = true)
    public long contarPendientesDeAprobacion() {

        return cotizacionRepositorio
                .countByEstadoCotizacion_NombreIgnoreCaseIn(
                        List.of(
                                ESTADO_PENDIENTE,
                                ESTADO_EN_PROCESO
                        )
                );
    }

    @Transactional
    public CotizacionRespuestaDTO registrarCotizacion(
            CotizacionRequestDTO datos) {

        validarDatosCotizacion(datos);

        Cliente cliente = clienteRepositorio
                .findById(datos.getIdCliente())
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró el cliente con ID: "
                        + datos.getIdCliente()
                ));

        Cotizacion cotizacion = new Cotizacion();

        cotizacion.setFecha(
                datos.getFecha()
        );

        cotizacion.setCliente(
                cliente
        );

        cotizacion.setEstadoCotizacion(
                buscarEstadoPorNombre(
                        ESTADO_PENDIENTE
                )
        );

        cotizacion.setSubtotal(
                BigDecimal.ZERO
        );

        cotizacion.setTotal(
                BigDecimal.ZERO
        );

        Cotizacion cotizacionGuardada =
                cotizacionRepositorio.save(
                        cotizacion
                );

        BigDecimal subtotal = guardarDetalles(
                cotizacionGuardada,
                datos.getDetalles()
        );

        aplicarTotales(
                cotizacionGuardada,
                subtotal
        );

        return mapearRespuesta(
                cotizacionRepositorio.save(
                        cotizacionGuardada
                )
        );
    }

    @Transactional
    public CotizacionRespuestaDTO actualizarCotizacion(
            Integer id,
            CotizacionRequestDTO datos) {

        validarDatosCotizacion(datos);

        Cotizacion cotizacion =
                buscarEntidadPorId(id);

        validarEditable(
                cotizacion
        );

        Cliente cliente = clienteRepositorio
                .findById(datos.getIdCliente())
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró el cliente con ID: "
                        + datos.getIdCliente()
                ));

        cotizacion.setFecha(
                datos.getFecha()
        );

        cotizacion.setCliente(
                cliente
        );

        detalleCotizacionRepositorio
                .deleteByCotizacion_IdCotizacion(
                        cotizacion.getIdCotizacion()
                );

        detalleCotizacionRepositorio.flush();

        BigDecimal subtotal = guardarDetalles(
                cotizacion,
                datos.getDetalles()
        );

        aplicarTotales(
                cotizacion,
                subtotal
        );

        return mapearRespuesta(
                cotizacionRepositorio.save(
                        cotizacion
                )
        );
    }

    @Transactional
    public void eliminarCotizacion(
            Integer id) {

        Cotizacion cotizacion =
                buscarEntidadPorId(id);

        validarEditable(
                cotizacion
        );

        detalleCotizacionRepositorio
                .deleteByCotizacion_IdCotizacion(
                        cotizacion.getIdCotizacion()
                );

        detalleCotizacionRepositorio.flush();

        cotizacionRepositorio.delete(
                cotizacion
        );

        cotizacionRepositorio.flush();
    }

    @Transactional
    public CotizacionRespuestaDTO aprobarCotizacion(
            Integer id) {

        Cotizacion cotizacion =
                buscarEntidadPorId(id);

        validarPendienteDeResolucion(
                cotizacion
        );

        cotizacion.setEstadoCotizacion(
                buscarEstadoPorNombre(
                        ESTADO_APROBADA
                )
        );

        return mapearRespuesta(
                cotizacionRepositorio.save(
                        cotizacion
                )
        );
    }

    @Transactional
    public CotizacionRespuestaDTO rechazarCotizacion(
            Integer id) {

        Cotizacion cotizacion =
                buscarEntidadPorId(id);

        validarPendienteDeResolucion(
                cotizacion
        );

        cotizacion.setEstadoCotizacion(
                buscarEstadoPorNombre(
                        ESTADO_RECHAZADA
                )
        );

        return mapearRespuesta(
                cotizacionRepositorio.save(
                        cotizacion
                )
        );
    }

    private void validarDatosCotizacion(
            CotizacionRequestDTO datos) {

        if (datos == null) {

            throw new IllegalArgumentException(
                    "Los datos de la cotización son obligatorios."
            );
        }

        if (datos.getIdCliente() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un cliente."
            );
        }

        if (datos.getFecha() == null) {

            throw new IllegalArgumentException(
                    "La fecha de la cotización es obligatoria."
            );
        }

        if (datos.getDetalles() == null
                || datos.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe agregar al menos un detalle."
            );
        }
    }

    private BigDecimal guardarDetalles(
            Cotizacion cotizacion,
            List<DetalleCotizacionItemDTO> items) {

        BigDecimal subtotal =
                BigDecimal.ZERO;

        for (DetalleCotizacionItemDTO item : items) {

            validarDetalle(
                    item
            );

            BigDecimal subtotalLinea =
                    item.getPrecio()
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.getCantidad()
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            DetalleCotizacion detalle =
                    new DetalleCotizacion();

            detalle.setDescripcion(
                    item.getDescripcion().trim()
            );

            detalle.setCantidad(
                    item.getCantidad()
            );

            detalle.setPrecio(
                    item.getPrecio()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
            );

            detalle.setSubtotal(
                    subtotalLinea
            );

            detalle.setCotizacion(
                    cotizacion
            );

            detalleCotizacionRepositorio.save(
                    detalle
            );

            subtotal = subtotal.add(
                    subtotalLinea
            );
        }

        return subtotal;
    }

    private void validarDetalle(
            DetalleCotizacionItemDTO item) {

        if (item == null) {

            throw new IllegalArgumentException(
                    "Uno de los detalles de la cotización no es válido."
            );
        }

        if (item.getDescripcion() == null
                || item.getDescripcion().isBlank()) {

            throw new IllegalArgumentException(
                    "La descripción de cada detalle es obligatoria."
            );
        }

        if (item.getCantidad() == null
                || item.getCantidad() < 1) {

            throw new IllegalArgumentException(
                    "La cantidad de cada detalle debe ser al menos 1."
            );
        }

        if (item.getPrecio() == null
                || item.getPrecio().compareTo(
                        BigDecimal.ZERO
                ) < 0) {

            throw new IllegalArgumentException(
                    "El precio de cada detalle no puede ser negativo."
            );
        }
    }

    private void aplicarTotales(
            Cotizacion cotizacion,
            BigDecimal subtotal) {

        BigDecimal subtotalCalculado =
                subtotal.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalCalculado =
                subtotalCalculado
                        .multiply(
                                FACTOR_IVA
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        cotizacion.setSubtotal(
                subtotalCalculado
        );

        cotizacion.setTotal(
                totalCalculado
        );
    }

    private void validarEditable(
            Cotizacion cotizacion) {

        String nombreEstado =
                cotizacion
                        .getEstadoCotizacion()
                        .getNombre();

        if (ESTADO_APROBADA.equalsIgnoreCase(
                nombreEstado
        )) {

            throw new IllegalArgumentException(
                    "No se puede modificar una cotización ya aprobada."
            );
        }
    }

    private void validarPendienteDeResolucion(
            Cotizacion cotizacion) {

        String nombreEstado =
                cotizacion
                        .getEstadoCotizacion()
                        .getNombre();

        boolean pendiente =
                ESTADO_PENDIENTE.equalsIgnoreCase(
                        nombreEstado
                );

        boolean enProceso =
                ESTADO_EN_PROCESO.equalsIgnoreCase(
                        nombreEstado
                );

        if (!pendiente && !enProceso) {

            throw new IllegalArgumentException(
                    "Solo se pueden aprobar o rechazar cotizaciones "
                    + "pendientes o en proceso."
            );
        }
    }

    private Cotizacion buscarEntidadPorId(
            Integer id) {

        return cotizacionRepositorio
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró la cotización con ID: "
                        + id
                ));
    }

    private EstadoCotizacion buscarEstadoPorNombre(
            String nombre) {

        return estadoCotizacionRepositorio
                .findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró el estado de cotización: "
                        + nombre
                ));
    }

    private CotizacionRespuestaDTO mapearRespuesta(
            Cotizacion cotizacion) {

        List<DetalleCotizacionRespuestaDTO> detalles =
                detalleCotizacionRepositorio
                        .findByCotizacion_IdCotizacion(
                                cotizacion.getIdCotizacion()
                        )
                        .stream()
                        .map(detalle ->
                                new DetalleCotizacionRespuestaDTO(
                                        detalle.getIdDetalle(),
                                        detalle.getDescripcion(),
                                        detalle.getCantidad(),
                                        detalle.getPrecio(),
                                        detalle.getSubtotal()
                                )
                        )
                        .toList();

        return new CotizacionRespuestaDTO(
                cotizacion.getIdCotizacion(),
                cotizacion.getFecha(),
                cotizacion.getSubtotal(),
                cotizacion.getTotal(),
                cotizacion
                        .getEstadoCotizacion()
                        .getNombre(),
                cotizacion
                        .getCliente()
                        .getIdCliente(),
                cotizacion
                        .getCliente()
                        .getNombre(),
                detalles
        );
    }
}
