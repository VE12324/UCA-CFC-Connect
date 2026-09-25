package sv.edu.udb.service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.PagoRequestDTO;
import sv.edu.udb.dto.PagoRespuestaDTO;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Cotizacion;
import sv.edu.udb.model.EstadoPago;
import sv.edu.udb.model.MetodoPago;
import sv.edu.udb.model.Pago;
import sv.edu.udb.model.TipoPago;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.CotizacionRepository;
import sv.edu.udb.repository.EstadoPagoRepository;
import sv.edu.udb.repository.MetodoPagoRepository;
import sv.edu.udb.repository.PagoRepository;


@Service
@RequiredArgsConstructor
public class PagoService {


    private static final String ESTADO_PENDIENTE = "Pendiente";
    private static final String ESTADO_PARCIAL = "Parcial";
    private static final String ESTADO_PAGADO = "Pagado";


    private final PagoRepository pagoRepositorio;
    private final EstadoPagoRepository estadoPagoRepositorio;
    private final MetodoPagoRepository metodoPagoRepositorio;
    private final ClienteRepository clienteRepositorio;
    private final CotizacionRepository cotizacionRepositorio;


    @Transactional(readOnly = true)
    public Page<PagoRespuestaDTO> listarPagos(String busqueda, Pageable paginacion) {
        Page<Pago> pagina = (busqueda == null || busqueda.isBlank())
                ? pagoRepositorio.findAll(paginacion)
                : pagoRepositorio.findByCliente_NombreContainingIgnoreCaseOrNumeroReferenciaExternaContainingIgnoreCase(
                busqueda, busqueda, paginacion);


        return pagina.map(this::mapearRespuesta);
    }


    @Transactional(readOnly = true)
    public PagoRespuestaDTO buscarPorId(Integer id) {
        return mapearRespuesta(buscarEntidadPorId(id));
    }


    @Transactional
    public PagoRespuestaDTO registrarPago(PagoRequestDTO datos) {
        Cliente cliente = clienteRepositorio.findById(datos.getIdCliente())
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el cliente con ID: " + datos.getIdCliente()));


        MetodoPago metodoPago = metodoPagoRepositorio.findById(datos.getIdMetodoPago())
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el método de pago con ID: " + datos.getIdMetodoPago()));


        Pago pago = new Pago();
        pago.setFecha(datos.getFecha());
        pago.setMonto(datos.getMonto());
        pago.setNumeroReferenciaExterna(datos.getNumeroReferenciaExterna());
        pago.setCliente(cliente);
        pago.setMetodoPago(metodoPago);
        pago.setEstadoPago(buscarEstadoPorNombre(ESTADO_PENDIENTE));
        asignarConcepto(pago, datos.getTipoPago(), datos.getIdReferencia());


        return mapearRespuesta(guardarValidandoReferencia(pago));
    }


    @Transactional
    public PagoRespuestaDTO actualizarPago(Integer id, PagoRequestDTO datos) {
        Pago pago = buscarEntidadPorId(id);


        validarEditable(pago);


        Cliente cliente = clienteRepositorio.findById(datos.getIdCliente())
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el cliente con ID: " + datos.getIdCliente()));


        MetodoPago metodoPago = metodoPagoRepositorio.findById(datos.getIdMetodoPago())
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el método de pago con ID: " + datos.getIdMetodoPago()));


        pago.setFecha(datos.getFecha());
        pago.setMonto(datos.getMonto());
        pago.setNumeroReferenciaExterna(datos.getNumeroReferenciaExterna());
        pago.setCliente(cliente);
        pago.setMetodoPago(metodoPago);
        asignarConcepto(pago, datos.getTipoPago(), datos.getIdReferencia());


        return mapearRespuesta(guardarValidandoReferencia(pago));
    }


    @Transactional
    public void eliminarPago(Integer id) {
        Pago pago = buscarEntidadPorId(id);
        validarEditable(pago);
        pagoRepositorio.delete(pago);
    }


    /**
     * Recalcula el estado del pago comparando el monto acumulado contra el total
     * de la cotización asociada. Si el pago no está ligado a una cotización, se
     * considera un pago único y completo (PAGADO).
     */
    @Transactional
    public PagoRespuestaDTO validarPago(Integer id) {
        Pago pago = buscarEntidadPorId(id);


        if (tieneEstado(pago, ESTADO_PAGADO)) {
            throw new IllegalArgumentException("El pago ya está validado como Pagado.");
        }


        if (pago.getCotizacion() == null) {
            pago.setEstadoPago(buscarEstadoPorNombre(ESTADO_PAGADO));
            return mapearRespuesta(pagoRepositorio.save(pago));
        }


        Cotizacion cotizacion = pago.getCotizacion();


        BigDecimal totalPagado = pagoRepositorio.findByCotizacion_IdCotizacion(cotizacion.getIdCotizacion())
                .stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        String nuevoEstado;
        if (totalPagado.compareTo(cotizacion.getTotal()) >= 0) {
            nuevoEstado = ESTADO_PAGADO;
        } else if (totalPagado.compareTo(BigDecimal.ZERO) > 0) {
            nuevoEstado = ESTADO_PARCIAL;
        } else {
            nuevoEstado = ESTADO_PENDIENTE;
        }


        pago.setEstadoPago(buscarEstadoPorNombre(nuevoEstado));


        return mapearRespuesta(pagoRepositorio.save(pago));
    }


    /**
     * Acción explícita para registrar que el pago cubre solo una parte del monto adeudado.
     */
    @Transactional
    public PagoRespuestaDTO marcarComoParcial(Integer id) {
        Pago pago = buscarEntidadPorId(id);


        if (tieneEstado(pago, ESTADO_PAGADO)) {
            throw new IllegalArgumentException("No se puede marcar como parcial un pago que ya está Pagado.");
        }
        if (tieneEstado(pago, ESTADO_PARCIAL)) {
            throw new IllegalArgumentException("El pago ya está marcado como Parcial.");
        }


        pago.setEstadoPago(buscarEstadoPorNombre(ESTADO_PARCIAL));
        return mapearRespuesta(pagoRepositorio.save(pago));
    }


    @Transactional
    public PagoRespuestaDTO emitirComprobante(Integer id) {
        Pago pago = buscarEntidadPorId(id);
        pago.setFechaEmision(LocalDate.now());
        return mapearRespuesta(pagoRepositorio.save(pago));
    }


    @Transactional(readOnly = true)
    public BigDecimal totalRecaudado() {
        return pagoRepositorio.sumarMontoPorEstado(ESTADO_PAGADO);
    }


    @Transactional(readOnly = true)
    public BigDecimal totalPendiente() {
        BigDecimal pendiente = pagoRepositorio.sumarMontoPorEstado(ESTADO_PENDIENTE);
        BigDecimal parcial = pagoRepositorio.sumarMontoPorEstado(ESTADO_PARCIAL);
        return pendiente.add(parcial);
    }


    @Transactional(readOnly = true)
    public long contarPagosPendientes() {
        return pagoRepositorio.countByEstadoPago_NombreIgnoreCase(ESTADO_PENDIENTE)
                + pagoRepositorio.countByEstadoPago_NombreIgnoreCase(ESTADO_PARCIAL);
    }


    @Transactional(readOnly = true)
    public long contarPagadosEsteMes() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        return pagoRepositorio.countByEstadoPago_NombreIgnoreCaseAndFechaBetween(ESTADO_PAGADO, inicioMes, finMes);
    }


    @Transactional(readOnly = true)
    public List<Object[]> desglosePorMetodo() {
        return pagoRepositorio.sumarMontoPorMetodo();
    }


    @Transactional(readOnly = true)
    public List<Object[]> desglosePorEstado() {
        return pagoRepositorio.sumarMontoPorEstadoAgrupado();
    }


    private void validarEditable(Pago pago) {
        if (tieneEstado(pago, ESTADO_PAGADO)) {
            throw new IllegalArgumentException("No se puede modificar un pago que ya fue marcado como pagado.");
        }
    }


    private boolean tieneEstado(Pago pago, String nombreEstado) {
        return nombreEstado.equalsIgnoreCase(pago.getEstadoPago().getNombre());
    }


    /**
     * Guarda el tipo de pago y coloca el idReferencia en la columna que le corresponde,
     * limpiando las demás. Solo Cotización usa relación JPA (módulo propio); inscripción,
     * alquiler y catering son IDs planos cuya existencia valida la FK de la BD.
     */
    private void asignarConcepto(Pago pago, TipoPago tipoPago, Integer idReferencia) {
        pago.setTipoPago(tipoPago);
        pago.setCotizacion(null);
        pago.setIdInscripcion(null);
        pago.setIdAlquiler(null);
        pago.setIdSolicitudCatering(null);


        switch (tipoPago) {
            case COTIZACION -> pago.setCotizacion(cotizacionRepositorio.findById(idReferencia)
                    .orElseThrow(() -> new IllegalArgumentException("No existe una cotización con ID " + idReferencia + ".")));
            case INSCRIPCION -> pago.setIdInscripcion(idReferencia);
            case ALQUILER -> pago.setIdAlquiler(idReferencia);
            case CATERING -> pago.setIdSolicitudCatering(idReferencia);
        }
    }


    private Pago guardarValidandoReferencia(Pago pago) {
        try {
            return pagoRepositorio.saveAndFlush(pago);
        } catch (DataIntegrityViolationException excepcion) {
            throw new IllegalArgumentException("No existe un registro de " + pago.getTipoPago().getEtiqueta().toLowerCase()
                    + " con ID " + obtenerIdReferencia(pago) + ".");
        }
    }


    // Los pagos anteriores a la columna tipo_pago no la tienen: se infiere de la columna de referencia llena
    private TipoPago resolverTipo(Pago pago) {
        if (pago.getTipoPago() != null) return pago.getTipoPago();
        if (pago.getIdInscripcion() != null) return TipoPago.INSCRIPCION;
        if (pago.getCotizacion() != null) return TipoPago.COTIZACION;
        if (pago.getIdAlquiler() != null) return TipoPago.ALQUILER;
        if (pago.getIdSolicitudCatering() != null) return TipoPago.CATERING;
        return null;
    }


    private Integer obtenerIdReferencia(Pago pago) {
        TipoPago tipo = resolverTipo(pago);
        if (tipo == null) return null;
        return switch (tipo) {
            case INSCRIPCION -> pago.getIdInscripcion();
            case COTIZACION -> pago.getCotizacion().getIdCotizacion();
            case ALQUILER -> pago.getIdAlquiler();
            case CATERING -> pago.getIdSolicitudCatering();
        };
    }


    private Pago buscarEntidadPorId(Integer id) {
        return pagoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el pago con ID: " + id));
    }


    private EstadoPago buscarEstadoPorNombre(String nombre) {
        return estadoPagoRepositorio.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el estado de pago: " + nombre));
    }


    private PagoRespuestaDTO mapearRespuesta(Pago pago) {
        return new PagoRespuestaDTO(
                pago.getIdPago(),
                pago.getFecha(),
                pago.getMonto(),
                resolverTipo(pago),
                obtenerIdReferencia(pago),
                pago.getNumeroReferenciaExterna(),
                pago.getEstadoPago().getNombre(),
                pago.getMetodoPago().getIdMetodoPago(),
                pago.getMetodoPago().getNombre(),
                pago.getCliente().getIdCliente(),
                pago.getCliente().getNombre(),
                pago.getCotizacion() != null ? pago.getCotizacion().getIdCotizacion() : null,
                pago.getFechaEmision()
        );
    }
}
