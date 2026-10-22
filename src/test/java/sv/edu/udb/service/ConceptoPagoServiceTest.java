package sv.edu.udb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.dto.ConceptoPagoDTO;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Cotizacion;
import sv.edu.udb.model.Curso;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.model.EstadoCotizacion;
import sv.edu.udb.model.EstadoInscripcion;
import sv.edu.udb.model.Inscripcion;
import sv.edu.udb.model.Participante;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.model.TipoPago;
import sv.edu.udb.repository.AlquilerRepository;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.CotizacionRepository;
import sv.edu.udb.repository.InscripcionRepository;
import sv.edu.udb.repository.PagoRepository;
import sv.edu.udb.repository.SolicitudCateringRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConceptoPagoService - conceptos pendientes de pago por cliente")
class ConceptoPagoServiceTest {

    private static final Integer ID_CLIENTE = 1;

    @Mock private ClienteRepository clienteRepository;
    @Mock private CotizacionRepository cotizacionRepository;
    @Mock private AlquilerRepository alquilerRepository;
    @Mock private SolicitudCateringRepository solicitudCateringRepository;
    @Mock private InscripcionRepository inscripcionRepository;
    @Mock private PagoRepository pagoRepository;

    @InjectMocks
    private ConceptoPagoService servicio;

    private Cliente cliente;

    @BeforeEach
    void prepararCliente() {
        cliente = new Cliente();
        cliente.setIdCliente(ID_CLIENTE);
        cliente.setNombre("Empresa Demo");
    }

    private void prepararConceptos(
            List<Cotizacion> cotizaciones,
            List<Alquiler> alquileres,
            List<SolicitudCatering> caterings,
            List<Inscripcion> inscripciones) {

        when(clienteRepository.existsById(ID_CLIENTE)).thenReturn(true);
        when(cotizacionRepository.findByCliente_IdCliente(ID_CLIENTE)).thenReturn(cotizaciones);
        when(alquilerRepository.findByCliente_IdCliente(ID_CLIENTE)).thenReturn(alquileres);
        when(solicitudCateringRepository.findByCliente_IdCliente(ID_CLIENTE)).thenReturn(caterings);
        when(inscripcionRepository.findByCliente_IdClienteOrderByFechaDesc(ID_CLIENTE))
                .thenReturn(inscripciones);
    }

    private Cotizacion cotizacion(Integer id, String estado, String total) {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setIdCotizacion(id);
        cotizacion.setFecha(LocalDate.of(2026, 9, 18));
        cotizacion.setTotal(new BigDecimal(total));
        cotizacion.setEstadoCotizacion(new EstadoCotizacion(1, estado));
        cotizacion.setCliente(cliente);
        return cotizacion;
    }

    private Alquiler alquiler(Integer id, String estado, LocalTime inicio, LocalTime fin) {
        Espacio espacio = new Espacio();
        espacio.setNombre("Auditorio Principal");
        espacio.setCostoHora(new BigDecimal("25.00"));

        LocalDate fecha = LocalDate.of(2026, 10, 25);
        Alquiler alquiler = new Alquiler();
        alquiler.setIdAlquiler(id);
        alquiler.setFecha(fecha);
        alquiler.setHoraInicio(LocalDateTime.of(fecha, inicio));
        alquiler.setHoraFin(LocalDateTime.of(fecha, fin));
        alquiler.setEstado(estado);
        alquiler.setCliente(cliente);
        alquiler.setEspacio(espacio);
        return alquiler;
    }

    private SolicitudCatering catering(Integer id, String estado, int asistentes) {
        ServicioCatering servicio = new ServicioCatering();
        servicio.setNombre("Coffee Break");
        servicio.setPrecioPorPersona(new BigDecimal("12.50"));

        SolicitudCatering solicitud = new SolicitudCatering();
        solicitud.setIdSolicitud(id);
        solicitud.setCantidadAsistentes(asistentes);
        solicitud.setFecha(LocalDate.of(2026, 10, 25));
        solicitud.setEstado(estado);
        solicitud.setCliente(cliente);
        solicitud.setServicioCatering(servicio);
        return solicitud;
    }

    private Inscripcion inscripcion(Integer id, String estado, int participantes) {
        Curso curso = new Curso();
        curso.setNombre("Excel avanzado");
        curso.setCosto(new BigDecimal("150.00"));

        Set<Participante> lista = new LinkedHashSet<>();
        for (int i = 1; i <= participantes; i++) {
            Participante participante = new Participante();
            participante.setIdParticipante(i);
            lista.add(participante);
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setIdInscripcion(id);
        inscripcion.setFecha(LocalDate.of(2026, 9, 18));
        inscripcion.setEstadoInscripcion(new EstadoInscripcion(1, estado));
        inscripcion.setCliente(cliente);
        inscripcion.setCurso(curso);
        inscripcion.setParticipantes(lista);
        return inscripcion;
    }

    @Test
    @DisplayName("Cotización Aprobada: saldo = total con IVA menos lo pagado; no aparecen las no aprobadas ni las pagadas")
    void cotizaciones() {
        // Arrange
        prepararConceptos(
                List.of(
                        cotizacion(1, "Aprobada", "339.00"),
                        cotizacion(2, "Pendiente", "565.00"),
                        cotizacion(3, "Aprobada", "113.00")
                ),
                List.of(), List.of(), List.of());
        when(pagoRepository.sumarPagadoCotizacion(1, null)).thenReturn(new BigDecimal("100.00"));
        when(pagoRepository.sumarPagadoCotizacion(2, null)).thenReturn(BigDecimal.ZERO);
        when(pagoRepository.sumarPagadoCotizacion(3, null)).thenReturn(new BigDecimal("113.00"));

        // Act
        List<ConceptoPagoDTO> conceptos = servicio.listarPendientes(ID_CLIENTE);

        // Assert
        assertThat(conceptos).hasSize(1);
        ConceptoPagoDTO concepto = conceptos.get(0);
        assertThat(concepto.tipoPago()).isEqualTo(TipoPago.COTIZACION);
        assertThat(concepto.idReferencia()).isEqualTo(1);
        assertThat(concepto.total()).isEqualByComparingTo("339.00");
        assertThat(concepto.pagado()).isEqualByComparingTo("100.00");
        assertThat(concepto.saldo()).isEqualByComparingTo("239.00");
        assertThat(concepto.descripcion()).contains("Cotización #1");
    }

    @Test
    @DisplayName("Alquiler: total = costo por hora × horas (con fracción); no aparecen los CANCELADO")
    void alquileres() {
        // Arrange
        prepararConceptos(
                List.of(),
                List.of(
                        alquiler(5, "PENDIENTE", LocalTime.of(8, 0), LocalTime.of(12, 30)),
                        alquiler(6, "CANCELADO", LocalTime.of(13, 0), LocalTime.of(17, 0))
                ),
                List.of(), List.of());
        when(pagoRepository.sumarPagadoAlquiler(anyInt(), isNull())).thenReturn(BigDecimal.ZERO);

        // Act
        List<ConceptoPagoDTO> conceptos = servicio.listarPendientes(ID_CLIENTE);

        // Assert
        assertThat(conceptos).extracting(ConceptoPagoDTO::idReferencia).containsExactly(5);
        assertThat(conceptos.get(0).total()).isEqualByComparingTo("112.50");
        assertThat(conceptos.get(0).saldo()).isEqualByComparingTo("112.50");
        assertThat(conceptos.get(0).descripcion()).contains("Auditorio Principal", "4.5 h");
    }

    @Test
    @DisplayName("Catering: total = precio por persona × asistentes; no aparecen las CANCELADA")
    void caterings() {
        // Arrange
        prepararConceptos(
                List.of(), List.of(),
                List.of(
                        catering(7, "CONFIRMADA", 60),
                        catering(8, "CANCELADA", 100)
                ),
                List.of());
        when(pagoRepository.sumarPagadoCatering(anyInt(), isNull())).thenReturn(BigDecimal.ZERO);

        // Act
        List<ConceptoPagoDTO> conceptos = servicio.listarPendientes(ID_CLIENTE);

        // Assert
        assertThat(conceptos).extracting(ConceptoPagoDTO::idReferencia).containsExactly(7);
        assertThat(conceptos.get(0).total()).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("Inscripción: total = costo del curso × participantes (mínimo 1); no aparecen las Cancelada")
    void inscripciones() {
        // Arrange
        prepararConceptos(
                List.of(), List.of(), List.of(),
                List.of(
                        inscripcion(9, "Confirmada", 2),
                        inscripcion(10, "Pendiente", 0),
                        inscripcion(11, "Cancelada", 3)
                ));
        when(pagoRepository.sumarPagadoInscripcion(anyInt(), isNull())).thenReturn(BigDecimal.ZERO);
        when(pagoRepository.sumarPagadoInscripcion(9, null)).thenReturn(new BigDecimal("50.00"));

        // Act
        List<ConceptoPagoDTO> conceptos = servicio.listarPendientes(ID_CLIENTE);

        // Assert
        assertThat(conceptos).extracting(ConceptoPagoDTO::idReferencia).containsExactly(9, 10);
        assertThat(conceptos.get(0).total()).isEqualByComparingTo("300.00");
        assertThat(conceptos.get(0).saldo()).isEqualByComparingTo("250.00");
        assertThat(conceptos.get(1).total()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Cliente inexistente: 404 sin consultar conceptos")
    void clienteInexistente() {
        when(clienteRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> servicio.listarPendientes(99))
                .isInstanceOf(RecursoNoEncontrado.class);
        verifyNoInteractions(cotizacionRepository, alquilerRepository,
                solicitudCateringRepository, inscripcionRepository, pagoRepository);
    }

    @Test
    @DisplayName("Obtener un concepto excluye de lo pagado el pago que se edita y marca si está vigente")
    void obtenerConceptoExcluyendoPago() {
        // Arrange
        when(cotizacionRepository.findById(2))
                .thenReturn(Optional.of(cotizacion(2, "Pendiente", "565.00")));
        when(pagoRepository.sumarPagadoCotizacion(2, 40)).thenReturn(new BigDecimal("65.00"));

        // Act
        ConceptoPagoDTO concepto = servicio.obtener(TipoPago.COTIZACION, 2, 40);

        // Assert
        assertThat(concepto.idCliente()).isEqualTo(ID_CLIENTE);
        assertThat(concepto.saldo()).isEqualByComparingTo("500.00");
        assertThat(concepto.vigente()).isFalse();
    }

    @Test
    @DisplayName("Obtener un concepto inexistente da un mensaje claro")
    void obtenerConceptoInexistente() {
        when(alquilerRepository.findById(77)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(TipoPago.ALQUILER, 77, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No existe un registro de alquiler de espacio con ID 77.");
    }
}
