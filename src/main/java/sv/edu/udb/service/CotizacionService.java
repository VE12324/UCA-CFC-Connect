package sv.edu.udb.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    private static final BigDecimal FACTOR_IVA = new BigDecimal("1.13");


    private static final String ESTADO_PENDIENTE = "Pendiente";
    private static final String ESTADO_EN_PROCESO = "En proceso";
    private static final String ESTADO_APROBADA = "Aprobada";
    private static final String ESTADO_RECHAZADA = "Rechazada";


    private final CotizacionRepository cotizacionRepositorio;
    private final DetalleCotizacionRepository detalleCotizacionRepositorio;
    private final EstadoCotizacionRepository estadoCotizacionRepositorio;
    private final ClienteRepository clienteRepositorio;


    @Transactional(readOnly = true)
    public Page<CotizacionRespuestaDTO> listarPendientesOEnProceso(Pageable paginacion) {
        return cotizacionRepositorio
                .findByEstadoCotizacion_NombreIgnoreCaseIn(List.of(ESTADO_PENDIENTE, ESTADO_EN_PROCESO), paginacion)
                .map(this::mapearRespuesta);
    }


    @Transactional(readOnly = true)
    public Page<CotizacionRespuestaDTO> listarTodas(Pageable paginacion) {
        return cotizacionRepositorio.findAll(paginacion).map(this::mapearRespuesta);
    }


    @Transactional(readOnly = true)
    public CotizacionRespuestaDTO buscarPorId(Integer id) {
        return mapearRespuesta(buscarEntidadPorId(id));
    }


    @Transactional(readOnly = true)
    public long contarPendientesDeAprobacion() {
        return cotizacionRepositorio.countByEstadoCotizacion_NombreIgnoreCaseIn(List.of(ESTADO_PENDIENTE, ESTADO_EN_PROCESO));
    }


    @Transactional
    public CotizacionRespuestaDTO registrarCotizacion(CotizacionRequestDTO datos) {
        Cliente cliente = clienteRepositorio.findById(datos.getIdCliente())
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el cliente con ID: " + datos.getIdCliente()));


        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setFecha(datos.getFecha());
        cotizacion.setCliente(cliente);
        cotizacion.setEstadoCotizacion(buscarEstadoPorNombre(ESTADO_PENDIENTE));
        cotizacion.setSubtotal(BigDecimal.ZERO);
        cotizacion.setTotal(BigDecimal.ZERO);


        Cotizacion cotizacionGuardada = cotizacionRepositorio.save(cotizacion);


        BigDecimal subtotal = guardarDetalles(cotizacionGuardada, datos.getDetalles());


        aplicarTotales(cotizacionGuardada, subtotal);


        return mapearRespuesta(cotizacionRepositorio.save(cotizacionGuardada));
    }


    @Transactional
    public CotizacionRespuestaDTO actualizarCotizacion(Integer id, CotizacionRequestDTO datos) {
        Cotizacion cotizacion = buscarEntidadPorId(id);


        validarEditable(cotizacion);


        Cliente cliente = clienteRepositorio.findById(datos.getIdCliente())
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el cliente con ID: " + datos.getIdCliente()));


        cotizacion.setFecha(datos.getFecha());
        cotizacion.setCliente(cliente);


        detalleCotizacionRepositorio.deleteByCotizacion_IdCotizacion(cotizacion.getIdCotizacion());
        detalleCotizacionRepositorio.flush();


        BigDecimal subtotal = guardarDetalles(cotizacion, datos.getDetalles());


        aplicarTotales(cotizacion, subtotal);


        return mapearRespuesta(cotizacionRepositorio.save(cotizacion));
    }


    @Transactional
    public void eliminarCotizacion(Integer id) {
        Cotizacion cotizacion = buscarEntidadPorId(id);
        validarEditable(cotizacion);
        cotizacionRepositorio.delete(cotizacion);
    }


    @Transactional
    public CotizacionRespuestaDTO aprobarCotizacion(Integer id) {
        Cotizacion cotizacion = buscarEntidadPorId(id);
        validarPendienteDeResolucion(cotizacion);
        cotizacion.setEstadoCotizacion(buscarEstadoPorNombre(ESTADO_APROBADA));
        return mapearRespuesta(cotizacionRepositorio.save(cotizacion));
    }


    @Transactional
    public CotizacionRespuestaDTO rechazarCotizacion(Integer id) {
        Cotizacion cotizacion = buscarEntidadPorId(id);
        validarPendienteDeResolucion(cotizacion);
        cotizacion.setEstadoCotizacion(buscarEstadoPorNombre(ESTADO_RECHAZADA));
        return mapearRespuesta(cotizacionRepositorio.save(cotizacion));
    }


    private BigDecimal guardarDetalles(Cotizacion cotizacion, List<DetalleCotizacionItemDTO> items) {
        BigDecimal subtotal = BigDecimal.ZERO;


        for (DetalleCotizacionItemDTO item : items) {
            BigDecimal subtotalLinea = item.getPrecio()
                    .multiply(BigDecimal.valueOf(item.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);


            DetalleCotizacion detalle = new DetalleCotizacion();
            detalle.setDescripcion(item.getDescripcion());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio(item.getPrecio());
            detalle.setSubtotal(subtotalLinea);
            detalle.setCotizacion(cotizacion);


            detalleCotizacionRepositorio.save(detalle);


            subtotal = subtotal.add(subtotalLinea);
        }


        return subtotal;
    }


    private void aplicarTotales(Cotizacion cotizacion, BigDecimal subtotal) {
        cotizacion.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        cotizacion.setTotal(subtotal.multiply(FACTOR_IVA).setScale(2, RoundingMode.HALF_UP));
    }


    private void validarEditable(Cotizacion cotizacion) {
        String nombreEstado = cotizacion.getEstadoCotizacion().getNombre();
        if (ESTADO_APROBADA.equalsIgnoreCase(nombreEstado)) {
            throw new IllegalArgumentException("No se puede modificar una cotización ya aprobada.");
        }
    }


    private void validarPendienteDeResolucion(Cotizacion cotizacion) {
        String nombreEstado = cotizacion.getEstadoCotizacion().getNombre();
        if (!ESTADO_PENDIENTE.equalsIgnoreCase(nombreEstado) && !ESTADO_EN_PROCESO.equalsIgnoreCase(nombreEstado)) {
            throw new IllegalArgumentException("Solo se pueden aprobar o rechazar cotizaciones pendientes o en proceso.");
        }
    }


    private Cotizacion buscarEntidadPorId(Integer id) {
        return cotizacionRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró la cotización con ID: " + id));
    }


    private EstadoCotizacion buscarEstadoPorNombre(String nombre) {
        return estadoCotizacionRepositorio.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el estado de cotización: " + nombre));
    }


    private CotizacionRespuestaDTO mapearRespuesta(Cotizacion cotizacion) {
        List<DetalleCotizacionRespuestaDTO> detalles = detalleCotizacionRepositorio
                .findByCotizacion_IdCotizacion(cotizacion.getIdCotizacion())
                .stream()
                .map(detalle -> new DetalleCotizacionRespuestaDTO(
                        detalle.getIdDetalle(),
                        detalle.getDescripcion(),
                        detalle.getCantidad(),
                        detalle.getPrecio(),
                        detalle.getSubtotal()
                ))
                .toList();


        return new CotizacionRespuestaDTO(
                cotizacion.getIdCotizacion(),
                cotizacion.getFecha(),
                cotizacion.getSubtotal(),
                cotizacion.getTotal(),
                cotizacion.getEstadoCotizacion().getNombre(),
                cotizacion.getCliente().getIdCliente(),
                cotizacion.getCliente().getNombre(),
                detalles
        );
    }
}
