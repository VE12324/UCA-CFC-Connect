package sv.edu.udb.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.CotizacionRequestDTO;
import sv.edu.udb.dto.CotizacionRespuestaDTO;
import sv.edu.udb.dto.DetalleCotizacionItemDTO;
import sv.edu.udb.dto.LineaCotizacionClienteDTO;
import sv.edu.udb.dto.PagoRespuestaDTO;
import sv.edu.udb.dto.ReservaOcupadaDTO;
import sv.edu.udb.dto.ResumenPagosDTO;
import sv.edu.udb.dto.SolicitudAlquilerDTO;
import sv.edu.udb.dto.SolicitudCateringClienteDTO;
import sv.edu.udb.dto.SolicitudCotizacionDTO;
import sv.edu.udb.exception.CampoInvalidoException;
import sv.edu.udb.exception.ClienteNoVinculadoException;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Curso;
import sv.edu.udb.model.Diplomado;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.repository.AlquilerRepository;
import sv.edu.udb.repository.CateringRepository;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.CursoRepository;
import sv.edu.udb.repository.DiplomadoRepository;
import sv.edu.udb.repository.EspacioRepository;

/**
 * Solicitudes que hace el cliente desde el portal: reservar un espacio, pedir catering y pedir una cotización.
 */
@Service
@RequiredArgsConstructor
public class SolicitudClienteService {

    public static final String TIPO_CURSO = "CURSO";
    public static final String TIPO_DIPLOMADO = "DIPLOMADO";
    public static final String TIPO_ESPACIO = "ESPACIO";
    public static final String TIPO_CATERING = "CATERING";
    public static final String TIPO_OTRO = "OTRO";

    private static final int MAX_DESCRIPCION = 255;
    private static final int MAX_TEXTO_LIBRE = 200;

    private final ClienteRepository clienteRepository;
    private final EspacioRepository espacioRepository;
    private final CateringRepository cateringRepository;
    private final CursoRepository cursoRepository;
    private final DiplomadoRepository diplomadoRepository;
    private final AlquilerRepository alquilerRepository;
    private final AlquilerService alquilerService;
    private final SolicitudCateringService solicitudCateringService;
    private final CotizacionService cotizacionService;
    private final PagoService pagoService;
    private final Clock reloj;

    // ------------------------------------------------------------------
    // Alquiler de espacios
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Espacio buscarEspacio(Integer idEspacio) {

        if (idEspacio == null) {
            throw new RecursoNoEncontrado("Debes elegir un espacio.");
        }

        return espacioRepository.findById(idEspacio)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "El espacio solicitado no existe."
                ));
    }

    /** Próximas reservas no canceladas del espacio (sin datos de clientes). */
    @Transactional(readOnly = true)
    public List<ReservaOcupadaDTO> listarProximasReservas(Integer idEspacio) {

        LocalDateTime ahora = LocalDateTime.now(reloj);

        return alquilerRepository.findAll(Sort.by("horaInicio").ascending())
                .stream()
                .filter(alquiler -> alquiler.getEspacio() != null
                        && idEspacio.equals(alquiler.getEspacio().getIdEspacio()))
                .filter(alquiler -> !"CANCELADO".equalsIgnoreCase(alquiler.getEstado()))
                .filter(alquiler -> alquiler.getHoraFin() != null
                        && alquiler.getHoraFin().isAfter(ahora))
                .limit(10)
                .map(alquiler -> new ReservaOcupadaDTO(
                        alquiler.getHoraInicio().toLocalDate(),
                        alquiler.getHoraInicio().toLocalTime(),
                        alquiler.getHoraFin().toLocalTime()
                ))
                .toList();
    }

    @Transactional
    public Alquiler solicitarAlquiler(
            String correoSesion,
            Integer idEspacio,
            SolicitudAlquilerDTO datos) {

        Cliente cliente = clienteDeSesion(correoSesion);
        Espacio espacio = buscarEspacio(idEspacio);

        validarNoPasado(datos.getFecha(), datos.getHoraInicio(), "horaInicio");

        if (!datos.getHoraFin().isAfter(datos.getHoraInicio())) {

            throw new CampoInvalidoException(
                    "horaFin",
                    "La hora de finalización debe ser posterior a la hora de inicio"
            );
        }

        if (datos.getCantidadPersonas() > espacio.getCapacidad()) {

            throw new CampoInvalidoException(
                    "cantidadPersonas",
                    "La cantidad de personas supera la capacidad del espacio seleccionado"
            );
        }

        Alquiler alquiler = new Alquiler();

        alquiler.setFecha(datos.getFecha());
        alquiler.setHoraInicio(datos.getFecha().atTime(datos.getHoraInicio()));
        alquiler.setHoraFin(datos.getFecha().atTime(datos.getHoraFin()));
        alquiler.setCantidadPersonas(datos.getCantidadPersonas());
        alquiler.setEstado("PENDIENTE");
        alquiler.setCliente(cliente);
        alquiler.setEspacio(espacio);

        // Choque de horario (EspacioOcupadoException), capacidad y
        // mantenimiento los vuelve a validar el servicio existente.
        return alquilerService.registrarAlquiler(alquiler);
    }

    // ------------------------------------------------------------------
    // Catering
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ServicioCatering buscarServicioCatering(Integer idServicio) {

        if (idServicio == null) {
            throw new RecursoNoEncontrado("Debes elegir un servicio de catering.");
        }

        return cateringRepository.findById(idServicio)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "El servicio de catering solicitado no existe."
                ));
    }

    @Transactional
    public SolicitudCatering solicitarCatering(
            String correoSesion,
            Integer idServicio,
            SolicitudCateringClienteDTO datos) {

        Cliente cliente = clienteDeSesion(correoSesion);
        ServicioCatering servicio = buscarServicioCatering(idServicio);

        validarNoPasado(datos.getFecha(), datos.getHora(), "hora");

        if (datos.getCantidadAsistentes() < servicio.getCapacidadMinima()) {

            throw new CampoInvalidoException(
                    "cantidadAsistentes",
                    "La cantidad de asistentes es menor que la capacidad mínima del servicio seleccionado"
            );
        }

        if (datos.getCantidadAsistentes() > servicio.getCapacidadMaxima()) {

            throw new CampoInvalidoException(
                    "cantidadAsistentes",
                    "La cantidad de asistentes supera la capacidad máxima del servicio seleccionado"
            );
        }

        String menu = datos.getMenu() == null || datos.getMenu().isBlank()
                ? servicio.getNombre()
                : datos.getMenu().trim();

        SolicitudCatering solicitud = new SolicitudCatering();

        solicitud.setFecha(datos.getFecha());
        solicitud.setHora(datos.getHora());
        solicitud.setLugar(datos.getLugar());
        solicitud.setCantidadAsistentes(datos.getCantidadAsistentes());
        solicitud.setMenu(menu);
        solicitud.setEstado("PENDIENTE");
        solicitud.setCliente(cliente);
        solicitud.setServicioCatering(servicio);

        // Rango de asistentes y servicio disponible los vuelve a validar el
        // servicio existente.
        return solicitudCateringService.registrarSolicitud(solicitud);
    }

    // ------------------------------------------------------------------
    // Cotización
    // ------------------------------------------------------------------

    @Transactional
    public CotizacionRespuestaDTO solicitarCotizacion(
            String correoSesion,
            SolicitudCotizacionDTO datos) {

        Cliente cliente = clienteDeSesion(correoSesion);

        List<LineaCotizacionClienteDTO> lineas = datos == null
                ? List.of()
                : datos.getLineas().stream()
                .filter(linea -> linea != null
                        && linea.getTipo() != null
                        && !linea.getTipo().isBlank())
                .toList();

        if (lineas.isEmpty()) {

            throw new IllegalArgumentException(
                    "Agrega al menos una línea a tu solicitud de cotización."
            );
        }

        List<DetalleCotizacionItemDTO> detalles = new ArrayList<>();

        for (int i = 0; i < lineas.size(); i++) {
            detalles.add(calcularDetalle(lineas.get(i), i + 1));
        }

        CotizacionRequestDTO solicitud = new CotizacionRequestDTO();

        solicitud.setIdCliente(cliente.getIdCliente());
        solicitud.setFecha(LocalDate.now(reloj));
        solicitud.setDetalles(detalles);

        // Subtotal, IVA 13 % y estado Pendiente los aplica CotizacionService.
        return cotizacionService.registrarCotizacion(solicitud);
    }

    /**
     * Convierte la línea del cliente en un detalle con descripción y precio
     * tomados del catálogo. Cualquier precio que mande el navegador se
     * ignora (el DTO ni siquiera lo tiene).
     */
    DetalleCotizacionItemDTO calcularDetalle(
            LineaCotizacionClienteDTO linea,
            int numero) {

        String prefijo = "Línea " + numero + ": ";

        if (linea.getCantidad() == null || linea.getCantidad() < 1) {

            throw new IllegalArgumentException(
                    prefijo + "la cantidad debe ser al menos 1."
            );
        }

        String tipo = linea.getTipo().trim().toUpperCase(Locale.ROOT);
        int cantidad = linea.getCantidad();

        String descripcion;
        BigDecimal precio;

        switch (tipo) {

            case TIPO_CURSO -> {
                Curso curso = cursoRepository.findById(requerirId(linea, prefijo))
                        .filter(c -> "ACTIVO".equalsIgnoreCase(c.getEstado()))
                        .orElseThrow(() -> new IllegalArgumentException(
                                prefijo + "el curso seleccionado no está disponible."));
                descripcion = "Curso: " + curso.getNombre()
                        + " (" + cantidad + " participante" + plural(cantidad) + ")";
                precio = curso.getCosto();
            }

            case TIPO_DIPLOMADO -> {
                Diplomado diplomado = diplomadoRepository.findById(requerirId(linea, prefijo))
                        .filter(d -> "ACTIVO".equalsIgnoreCase(d.getEstado()))
                        .orElseThrow(() -> new IllegalArgumentException(
                                prefijo + "el diplomado seleccionado no está disponible."));
                descripcion = "Diplomado: " + diplomado.getNombre()
                        + " (" + cantidad + " participante" + plural(cantidad) + ")";
                precio = diplomado.getCosto();
            }

            case TIPO_ESPACIO -> {
                Espacio espacio = espacioRepository.findById(requerirId(linea, prefijo))
                        .filter(PortalPublicoService::espacioReservable)
                        .orElseThrow(() -> new IllegalArgumentException(
                                prefijo + "el espacio seleccionado no está disponible."));
                descripcion = "Alquiler de espacio: " + espacio.getNombre()
                        + " (" + cantidad + " hora" + plural(cantidad) + ")";
                precio = espacio.getCostoHora();
            }

            case TIPO_CATERING -> {
                ServicioCatering servicio = cateringRepository.findById(requerirId(linea, prefijo))
                        .filter(PortalPublicoService::cateringDisponible)
                        .orElseThrow(() -> new IllegalArgumentException(
                                prefijo + "el servicio de catering seleccionado no está disponible."));
                descripcion = "Catering: " + servicio.getNombre()
                        + " (" + cantidad + " persona" + plural(cantidad) + ")";
                precio = servicio.getPrecioPorPersona();
            }

            case TIPO_OTRO -> {
                String texto = linea.getDescripcion() == null
                        ? ""
                        : linea.getDescripcion().trim();
                if (texto.isEmpty()) {
                    throw new IllegalArgumentException(
                            prefijo + "describe lo que necesitas cotizar.");
                }
                if (texto.length() > MAX_TEXTO_LIBRE) {
                    throw new IllegalArgumentException(
                            prefijo + "la descripción no puede superar los "
                                    + MAX_TEXTO_LIBRE + " caracteres.");
                }
                // Lo cotiza el personal del centro.
                descripcion = "Otro: " + texto;
                precio = BigDecimal.ZERO;
            }

            default -> throw new IllegalArgumentException(
                    prefijo + "el tipo seleccionado no es válido.");
        }

        DetalleCotizacionItemDTO detalle = new DetalleCotizacionItemDTO();

        detalle.setDescripcion(recortar(descripcion));
        detalle.setCantidad(cantidad);
        detalle.setPrecio(precio);

        return detalle;
    }

    // ------------------------------------------------------------------
    // Pagos
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ResumenPagosDTO resumenPagos(String correoSesion) {

        List<PagoRespuestaDTO> pagos = pagoService
                .listarPorCorreoCliente(correoSesion, Pageable.unpaged())
                .getContent();

        BigDecimal pagado = BigDecimal.ZERO;
        BigDecimal porValidar = BigDecimal.ZERO;

        for (PagoRespuestaDTO pago : pagos) {

            BigDecimal monto = pago.monto() == null ? BigDecimal.ZERO : pago.monto();
            String estado = pago.estado() == null ? "" : pago.estado();

            if ("Pagado".equalsIgnoreCase(estado) || "Parcial".equalsIgnoreCase(estado)) {
                pagado = pagado.add(monto);
            } else if ("Pendiente".equalsIgnoreCase(estado)) {
                porValidar = porValidar.add(monto);
            }
        }

        return new ResumenPagosDTO(pagado, porValidar, pagos.size());
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private Cliente clienteDeSesion(String correoSesion) {

        if (correoSesion == null || correoSesion.isBlank()) {
            throw new ClienteNoVinculadoException();
        }

        return clienteRepository
                .findByUsuario_EmailIgnoreCase(correoSesion.trim())
                .orElseThrow(ClienteNoVinculadoException::new);
    }

    /** La fecha no puede ser anterior a hoy y, si es hoy, la hora no puede haber pasado. */
    private void validarNoPasado(LocalDate fecha, LocalTime hora, String campoHora) {

        LocalDateTime ahora = LocalDateTime.now(reloj);

        if (fecha.isBefore(ahora.toLocalDate())) {

            throw new CampoInvalidoException(
                    "fecha",
                    "La fecha no puede ser anterior a hoy"
            );
        }

        if (fecha.isEqual(ahora.toLocalDate())
                && !hora.isAfter(ahora.toLocalTime())) {

            throw new CampoInvalidoException(
                    campoHora,
                    "Esa hora ya pasó; elige una hora posterior a la actual"
            );
        }
    }

    private static Integer requerirId(LineaCotizacionClienteDTO linea, String prefijo) {

        if (linea.getIdItem() == null) {
            throw new IllegalArgumentException(prefijo + "selecciona una opción del catálogo.");
        }

        return linea.getIdItem();
    }

    private static String plural(int cantidad) {
        return cantidad == 1 ? "" : "s";
    }

    private static String recortar(String texto) {
        return texto.length() <= MAX_DESCRIPCION
                ? texto
                : texto.substring(0, MAX_DESCRIPCION);
    }
}
