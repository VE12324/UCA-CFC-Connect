package sv.edu.udb.service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.ConceptoPagoDTO;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Cotizacion;
import sv.edu.udb.model.Inscripcion;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.model.TipoPago;
import sv.edu.udb.repository.AlquilerRepository;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.CotizacionRepository;
import sv.edu.udb.repository.InscripcionRepository;
import sv.edu.udb.repository.PagoRepository;
import sv.edu.udb.repository.SolicitudCateringRepository;


/**
 * Conceptos que un cliente puede pagar y su saldo pendiente.
 *
 * Totales (no hay un total guardado salvo en la cotización):
 * - Cotización Aprobada: cotizacion.total (con IVA).
 * - Alquiler no CANCELADO: costo por hora del espacio × horas reservadas.
 * - Catering no CANCELADA: precio por persona × asistentes.
 * - Inscripción no Cancelada: costo del curso × participantes (mínimo 1).
 *
 * Lo pagado suma todos los pagos del concepto, en cualquier estado, usando
 * las columnas de referencia de la tabla pago.
 */
@Service
@RequiredArgsConstructor
public class ConceptoPagoService {


    static final String COTIZACION_APROBADA = "Aprobada";
    static final String ALQUILER_CANCELADO = "CANCELADO";
    static final String CATERING_CANCELADA = "CANCELADA";
    static final String INSCRIPCION_CANCELADA = "Cancelada";


    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final BigDecimal MINUTOS_POR_HORA = BigDecimal.valueOf(60);


    private final ClienteRepository clienteRepository;
    private final CotizacionRepository cotizacionRepository;
    private final AlquilerRepository alquilerRepository;
    private final SolicitudCateringRepository solicitudCateringRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PagoRepository pagoRepository;


    /** Conceptos vigentes del cliente con saldo mayor que cero. */
    @Transactional(readOnly = true)
    public List<ConceptoPagoDTO> listarPendientes(Integer idCliente) {


        if (idCliente == null || !clienteRepository.existsById(idCliente)) {
            throw new RecursoNoEncontrado(
                    "No se encontró el cliente con ID: " + idCliente
            );
        }


        List<ConceptoPagoDTO> conceptos = new ArrayList<>();


        cotizacionRepository.findByCliente_IdCliente(idCliente)
                .forEach(cotizacion -> conceptos.add(deCotizacion(cotizacion, null)));


        alquilerRepository.findByCliente_IdCliente(idCliente)
                .forEach(alquiler -> conceptos.add(deAlquiler(alquiler, null)));


        solicitudCateringRepository.findByCliente_IdCliente(idCliente)
                .forEach(solicitud -> conceptos.add(deCatering(solicitud, null)));


        inscripcionRepository.findByCliente_IdClienteOrderByFechaDesc(idCliente)
                .forEach(inscripcion -> conceptos.add(deInscripcion(inscripcion, null)));


        return conceptos.stream()
                .filter(ConceptoPagoDTO::vigente)
                .filter(concepto -> concepto.saldo().signum() > 0)
                .toList();
    }


    /**
     * Un concepto por tipo e ID, aunque no esté vigente o ya esté pagado.
     * idPagoExcluido deja fuera de "pagado" al pago que se está editando.
     */
    @Transactional(readOnly = true)
    public ConceptoPagoDTO obtener(
            TipoPago tipo,
            Integer idReferencia,
            Integer idPagoExcluido) {


        if (tipo == null || idReferencia == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar qué se está pagando."
            );
        }


        return switch (tipo) {


            case COTIZACION -> cotizacionRepository.findById(idReferencia)
                    .map(cotizacion -> deCotizacion(cotizacion, idPagoExcluido))
                    .orElseThrow(() -> noExiste(tipo, idReferencia));


            case ALQUILER -> alquilerRepository.findById(idReferencia)
                    .map(alquiler -> deAlquiler(alquiler, idPagoExcluido))
                    .orElseThrow(() -> noExiste(tipo, idReferencia));


            case CATERING -> solicitudCateringRepository.findById(idReferencia)
                    .map(solicitud -> deCatering(solicitud, idPagoExcluido))
                    .orElseThrow(() -> noExiste(tipo, idReferencia));


            case INSCRIPCION -> inscripcionRepository.findById(idReferencia)
                    .map(inscripcion -> deInscripcion(inscripcion, idPagoExcluido))
                    .orElseThrow(() -> noExiste(tipo, idReferencia));
        };
    }


    private ConceptoPagoDTO deCotizacion(Cotizacion cotizacion, Integer idPagoExcluido) {


        Integer id = cotizacion.getIdCotizacion();
        String estado = cotizacion.getEstadoCotizacion() != null
                ? cotizacion.getEstadoCotizacion().getNombre()
                : null;


        return armar(
                TipoPago.COTIZACION,
                id,
                cotizacion.getCliente().getIdCliente(),
                "Cotización #" + id + " del " + cotizacion.getFecha().format(FECHA),
                cotizacion.getTotal(),
                pagoRepository.sumarPagadoCotizacion(id, idPagoExcluido),
                COTIZACION_APROBADA.equalsIgnoreCase(estado)
        );
    }


    private ConceptoPagoDTO deAlquiler(Alquiler alquiler, Integer idPagoExcluido) {


        Integer id = alquiler.getIdAlquiler();
        long minutos = Duration.between(
                alquiler.getHoraInicio(),
                alquiler.getHoraFin()
        ).toMinutes();


        BigDecimal horas = BigDecimal.valueOf(minutos)
                .divide(MINUTOS_POR_HORA, 2, RoundingMode.HALF_UP);


        BigDecimal total = valor(alquiler.getEspacio().getCostoHora())
                .multiply(BigDecimal.valueOf(minutos))
                .divide(MINUTOS_POR_HORA, 2, RoundingMode.HALF_UP);


        return armar(
                TipoPago.ALQUILER,
                id,
                alquiler.getCliente().getIdCliente(),
                "Alquiler #" + id + " · " + alquiler.getEspacio().getNombre()
                        + " · " + alquiler.getHoraInicio().format(FECHA)
                        + " " + alquiler.getHoraInicio().format(HORA)
                        + "–" + alquiler.getHoraFin().format(HORA)
                        + " (" + horas.stripTrailingZeros().toPlainString() + " h)",
                total,
                pagoRepository.sumarPagadoAlquiler(id, idPagoExcluido),
                !ALQUILER_CANCELADO.equalsIgnoreCase(alquiler.getEstado())
        );
    }


    private ConceptoPagoDTO deCatering(SolicitudCatering solicitud, Integer idPagoExcluido) {


        Integer id = solicitud.getIdSolicitud();
        int asistentes = solicitud.getCantidadAsistentes() != null
                ? solicitud.getCantidadAsistentes()
                : 0;


        return armar(
                TipoPago.CATERING,
                id,
                solicitud.getCliente().getIdCliente(),
                "Catering #" + id + " · " + solicitud.getServicioCatering().getNombre()
                        + " · " + asistentes + " asistentes · "
                        + solicitud.getFecha().format(FECHA),
                valor(solicitud.getServicioCatering().getPrecioPorPersona())
                        .multiply(BigDecimal.valueOf(asistentes)),
                pagoRepository.sumarPagadoCatering(id, idPagoExcluido),
                !CATERING_CANCELADA.equalsIgnoreCase(solicitud.getEstado())
        );
    }


    private ConceptoPagoDTO deInscripcion(Inscripcion inscripcion, Integer idPagoExcluido) {


        Integer id = inscripcion.getIdInscripcion();
        int participantes = Math.max(
                1,
                inscripcion.getParticipantes() != null
                        ? inscripcion.getParticipantes().size()
                        : 0
        );
        String estado = inscripcion.getEstadoInscripcion() != null
                ? inscripcion.getEstadoInscripcion().getNombre()
                : null;


        return armar(
                TipoPago.INSCRIPCION,
                id,
                inscripcion.getCliente().getIdCliente(),
                "Inscripción #" + id + " · " + inscripcion.getCurso().getNombre()
                        + " · " + participantes
                        + (participantes == 1 ? " participante" : " participantes"),
                valor(inscripcion.getCurso().getCosto())
                        .multiply(BigDecimal.valueOf(participantes)),
                pagoRepository.sumarPagadoInscripcion(id, idPagoExcluido),
                !INSCRIPCION_CANCELADA.equalsIgnoreCase(estado)
        );
    }


    private static ConceptoPagoDTO armar(
            TipoPago tipo,
            Integer id,
            Integer idCliente,
            String descripcion,
            BigDecimal total,
            BigDecimal pagado,
            boolean vigente) {


        BigDecimal totalRedondeado = valor(total).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pagadoRedondeado = valor(pagado).setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldo = totalRedondeado.subtract(pagadoRedondeado).max(BigDecimal.ZERO);


        return new ConceptoPagoDTO(
                tipo,
                tipo.getEtiqueta(),
                id,
                idCliente,
                descripcion,
                totalRedondeado,
                pagadoRedondeado,
                saldo,
                vigente
        );
    }


    private static BigDecimal valor(BigDecimal numero) {
        return numero == null ? BigDecimal.ZERO : numero;
    }


    private static IllegalArgumentException noExiste(TipoPago tipo, Integer id) {
        return new IllegalArgumentException(
                "No existe un registro de " + tipo.getEtiqueta().toLowerCase()
                        + " con ID " + id + "."
        );
    }
}

