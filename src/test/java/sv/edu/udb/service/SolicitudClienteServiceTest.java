package sv.edu.udb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.dto.CotizacionRequestDTO;
import sv.edu.udb.dto.DetalleCotizacionItemDTO;
import sv.edu.udb.dto.LineaCotizacionClienteDTO;
import sv.edu.udb.dto.SolicitudAlquilerDTO;
import sv.edu.udb.dto.SolicitudCateringClienteDTO;
import sv.edu.udb.dto.SolicitudCotizacionDTO;
import sv.edu.udb.exception.CampoInvalidoException;
import sv.edu.udb.exception.ClienteNoVinculadoException;
import sv.edu.udb.exception.EspacioOcupadoException;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Curso;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.repository.AlquilerRepository;
import sv.edu.udb.repository.CateringRepository;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.CursoRepository;
import sv.edu.udb.repository.DiplomadoRepository;
import sv.edu.udb.repository.EspacioRepository;

@ExtendWith(MockitoExtension.class)
class SolicitudClienteServiceTest {

    private static final String CORREO = "cliente@prueba.local";
    private static final LocalDate HOY = LocalDate.of(2026, 10, 20);
    private static final LocalDate MANANA = HOY.plusDays(1);

    @Mock private ClienteRepository clienteRepository;
    @Mock private EspacioRepository espacioRepository;
    @Mock private CateringRepository cateringRepository;
    @Mock private CursoRepository cursoRepository;
    @Mock private DiplomadoRepository diplomadoRepository;
    @Mock private AlquilerRepository alquilerRepository;
    @Mock private AlquilerService alquilerService;
    @Mock private SolicitudCateringService solicitudCateringService;
    @Mock private CotizacionService cotizacionService;
    @Mock private PagoService pagoService;

    private SolicitudClienteService servicio;
    private Cliente cliente;

    @BeforeEach
    void setUp() {

        Clock reloj = Clock.fixed(
                Instant.parse("2026-10-20T10:00:00Z"),
                ZoneOffset.UTC
        );

        servicio = new SolicitudClienteService(
                clienteRepository, espacioRepository, cateringRepository,
                cursoRepository, diplomadoRepository, alquilerRepository,
                alquilerService, solicitudCateringService, cotizacionService,
                pagoService, reloj
        );

        cliente = new Cliente(7, null, "Ana López", null, CORREO, null, null);
    }

    private void clienteEnSesion() {
        when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                .thenReturn(Optional.of(cliente));
    }

    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Alquiler de espacios")
    class AlquilerEspacios {

        private Espacio espacio;

        @BeforeEach
        void setUp() {
            espacio = new Espacio(2, "Sala A", "Sala", 80, "Edificio B",
                    new BigDecimal("250.00"), "DISPONIBLE");
        }

        @Test
        @DisplayName("Éxito: queda PENDIENTE, con el cliente de la sesión y pasa por AlquilerService")
        void exito() {

            clienteEnSesion();
            when(espacioRepository.findById(2)).thenReturn(Optional.of(espacio));
            when(alquilerService.registrarAlquiler(any(Alquiler.class)))
                    .thenAnswer(invocacion -> invocacion.getArgument(0));

            Alquiler alquiler = servicio.solicitarAlquiler(CORREO, 2,
                    new SolicitudAlquilerDTO(MANANA, LocalTime.of(9, 0), LocalTime.of(11, 0), 30));

            assertThat(alquiler.getEstado()).isEqualTo("PENDIENTE");
            assertThat(alquiler.getCliente()).isSameAs(cliente);
            assertThat(alquiler.getEspacio()).isSameAs(espacio);
            assertThat(alquiler.getFecha()).isEqualTo(MANANA);
            assertThat(alquiler.getHoraInicio()).isEqualTo(LocalDateTime.of(MANANA, LocalTime.of(9, 0)));
            assertThat(alquiler.getHoraFin()).isEqualTo(LocalDateTime.of(MANANA, LocalTime.of(11, 0)));
            verify(alquilerService).registrarAlquiler(alquiler);
        }

        @Test
        @DisplayName("Choque de horario: propaga EspacioOcupadoException de AlquilerService")
        void choqueDeHorario() {

            clienteEnSesion();
            when(espacioRepository.findById(2)).thenReturn(Optional.of(espacio));
            when(alquilerService.registrarAlquiler(any(Alquiler.class)))
                    .thenThrow(new EspacioOcupadoException(
                            "El espacio ya está reservado durante el horario seleccionado"));

            assertThatThrownBy(() -> servicio.solicitarAlquiler(CORREO, 2,
                    new SolicitudAlquilerDTO(MANANA, LocalTime.of(9, 0), LocalTime.of(11, 0), 30)))
                    .isInstanceOf(EspacioOcupadoException.class)
                    .hasMessage("El espacio ya está reservado durante el horario seleccionado");
        }

        @Test
        @DisplayName("Personas por encima de la capacidad: se rechaza sin registrar")
        void superaCapacidad() {

            clienteEnSesion();
            when(espacioRepository.findById(2)).thenReturn(Optional.of(espacio));

            assertThatThrownBy(() -> servicio.solicitarAlquiler(CORREO, 2,
                    new SolicitudAlquilerDTO(MANANA, LocalTime.of(9, 0), LocalTime.of(11, 0), 81)))
                    .isInstanceOf(CampoInvalidoException.class)
                    .hasMessageContaining("supera la capacidad")
                    .extracting("campo").isEqualTo("cantidadPersonas");

            verify(alquilerService, never()).registrarAlquiler(any());
        }

        @Test
        @DisplayName("Fecha pasada: se rechaza en el campo fecha")
        void fechaPasada() {

            clienteEnSesion();
            when(espacioRepository.findById(2)).thenReturn(Optional.of(espacio));

            assertThatThrownBy(() -> servicio.solicitarAlquiler(CORREO, 2,
                    new SolicitudAlquilerDTO(HOY.minusDays(1), LocalTime.of(9, 0), LocalTime.of(11, 0), 30)))
                    .isInstanceOf(CampoInvalidoException.class)
                    .hasMessage("La fecha no puede ser anterior a hoy")
                    .extracting("campo").isEqualTo("fecha");

            verify(alquilerService, never()).registrarAlquiler(any());
        }

        @Test
        @DisplayName("Hoy con una hora que ya pasó: se rechaza en la hora de inicio")
        void horaPasadaHoy() {

            clienteEnSesion();
            when(espacioRepository.findById(2)).thenReturn(Optional.of(espacio));

            assertThatThrownBy(() -> servicio.solicitarAlquiler(CORREO, 2,
                    new SolicitudAlquilerDTO(HOY, LocalTime.of(9, 30), LocalTime.of(11, 0), 30)))
                    .isInstanceOf(CampoInvalidoException.class)
                    .extracting("campo").isEqualTo("horaInicio");
        }

        @Test
        @DisplayName("Cuenta CLIENTE sin cliente vinculado: ClienteNoVinculadoException")
        void cuentaSinClienteVinculado() {

            when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicio.solicitarAlquiler(CORREO, 2,
                    new SolicitudAlquilerDTO(MANANA, LocalTime.of(9, 0), LocalTime.of(11, 0), 30)))
                    .isInstanceOf(ClienteNoVinculadoException.class)
                    .hasMessageContaining("Comunícate con recepción");

            verifyNoInteractions(alquilerService, espacioRepository);
        }
    }

    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Catering")
    class Catering {

        private ServicioCatering coffee;

        @BeforeEach
        void setUp() {
            coffee = new ServicioCatering(1, "Coffee Break", "Café y bocadillos",
                    new BigDecimal("10.00"), 20, 150, "DISPONIBLE");
        }

        @Test
        @DisplayName("Éxito: PENDIENTE, cliente de la sesión y menú vacío → nombre del servicio")
        void exito() {

            clienteEnSesion();
            when(cateringRepository.findById(1)).thenReturn(Optional.of(coffee));
            when(solicitudCateringService.registrarSolicitud(any(SolicitudCatering.class)))
                    .thenAnswer(invocacion -> invocacion.getArgument(0));

            SolicitudCatering solicitud = servicio.solicitarCatering(CORREO, 1,
                    new SolicitudCateringClienteDTO(MANANA, LocalTime.of(10, 0),
                            "Auditorio Principal", 40, "  "));

            assertThat(solicitud.getEstado()).isEqualTo("PENDIENTE");
            assertThat(solicitud.getCliente()).isSameAs(cliente);
            assertThat(solicitud.getServicioCatering()).isSameAs(coffee);
            assertThat(solicitud.getMenu()).isEqualTo("Coffee Break");
            assertThat(solicitud.getCantidadAsistentes()).isEqualTo(40);
            verify(solicitudCateringService).registrarSolicitud(solicitud);
        }

        @Test
        @DisplayName("Asistentes por debajo del mínimo o por encima del máximo: se rechaza")
        void asistentesFueraDeRango() {

            clienteEnSesion();
            when(cateringRepository.findById(1)).thenReturn(Optional.of(coffee));

            assertThatThrownBy(() -> servicio.solicitarCatering(CORREO, 1,
                    new SolicitudCateringClienteDTO(MANANA, LocalTime.of(10, 0), "Sala A", 10, null)))
                    .isInstanceOf(CampoInvalidoException.class)
                    .hasMessageContaining("capacidad mínima")
                    .extracting("campo").isEqualTo("cantidadAsistentes");

            assertThatThrownBy(() -> servicio.solicitarCatering(CORREO, 1,
                    new SolicitudCateringClienteDTO(MANANA, LocalTime.of(10, 0), "Sala A", 151, null)))
                    .isInstanceOf(CampoInvalidoException.class)
                    .hasMessageContaining("capacidad máxima");

            verify(solicitudCateringService, never()).registrarSolicitud(any());
        }

        @Test
        @DisplayName("Fecha pasada: se rechaza en el campo fecha")
        void fechaPasada() {

            clienteEnSesion();
            when(cateringRepository.findById(1)).thenReturn(Optional.of(coffee));

            assertThatThrownBy(() -> servicio.solicitarCatering(CORREO, 1,
                    new SolicitudCateringClienteDTO(HOY.minusDays(3), LocalTime.of(10, 0), "Sala A", 40, null)))
                    .isInstanceOf(CampoInvalidoException.class)
                    .extracting("campo").isEqualTo("fecha");

            verify(solicitudCateringService, never()).registrarSolicitud(any());
        }
    }

    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Cotización")
    class Cotizacion {

        @Test
        @DisplayName("Los precios y descripciones salen del catálogo; el cliente, de la sesión; la fecha es hoy")
        void preciosDelCatalogo() {

            clienteEnSesion();

            Curso curso = new Curso();
            curso.setIdCurso(1);
            curso.setNombre("Programación con Java");
            curso.setCosto(new BigDecimal("150.00"));
            curso.setEstado("ACTIVO");

            Espacio espacio = new Espacio(2, "Sala A", null, 80, null,
                    new BigDecimal("250.00"), "DISPONIBLE");

            ServicioCatering coffee = new ServicioCatering(1, "Coffee Break", null,
                    new BigDecimal("10.00"), 20, 150, "DISPONIBLE");

            when(cursoRepository.findById(1)).thenReturn(Optional.of(curso));
            when(espacioRepository.findById(2)).thenReturn(Optional.of(espacio));
            when(cateringRepository.findById(1)).thenReturn(Optional.of(coffee));

            SolicitudCotizacionDTO solicitud = new SolicitudCotizacionDTO();
            solicitud.setLineas(List.of(
                    new LineaCotizacionClienteDTO("CURSO", 1, 3, "Me lo dejan en $1"),
                    new LineaCotizacionClienteDTO("ESPACIO", 2, 4, null),
                    new LineaCotizacionClienteDTO("CATERING", 1, 30, null),
                    new LineaCotizacionClienteDTO("OTRO", null, 1, "Curso empresarial de Excel")
            ));

            servicio.solicitarCotizacion(CORREO, solicitud);

            ArgumentCaptor<CotizacionRequestDTO> captor =
                    ArgumentCaptor.forClass(CotizacionRequestDTO.class);
            verify(cotizacionService).registrarCotizacion(captor.capture());

            CotizacionRequestDTO enviada = captor.getValue();
            List<DetalleCotizacionItemDTO> detalles = enviada.getDetalles();

            assertThat(enviada.getIdCliente()).isEqualTo(7);
            assertThat(enviada.getFecha()).isEqualTo(HOY);
            assertThat(detalles).extracting(DetalleCotizacionItemDTO::getPrecio)
                    .containsExactly(new BigDecimal("150.00"), new BigDecimal("250.00"),
                            new BigDecimal("10.00"), BigDecimal.ZERO);
            assertThat(detalles).extracting(DetalleCotizacionItemDTO::getCantidad)
                    .containsExactly(3, 4, 30, 1);
            // La descripción del curso la arma el servidor: el texto del cliente se ignora.
            assertThat(detalles.get(0).getDescripcion())
                    .isEqualTo("Curso: Programación con Java (3 participantes)");
            assertThat(detalles.get(3).getDescripcion())
                    .isEqualTo("Otro: Curso empresarial de Excel");
        }

        @Test
        @DisplayName("Sin líneas: se rechaza y no se crea la cotización")
        void sinLineas() {

            clienteEnSesion();

            SolicitudCotizacionDTO solicitud = new SolicitudCotizacionDTO();
            solicitud.setLineas(List.of(new LineaCotizacionClienteDTO()));

            assertThatThrownBy(() -> servicio.solicitarCotizacion(CORREO, solicitud))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Agrega al menos una línea a tu solicitud de cotización.");

            verifyNoInteractions(cotizacionService);
        }

        @Test
        @DisplayName("Un curso inactivo no se puede cotizar")
        void cursoInactivo() {

            clienteEnSesion();

            Curso curso = new Curso();
            curso.setIdCurso(3);
            curso.setEstado("INACTIVO");
            when(cursoRepository.findById(3)).thenReturn(Optional.of(curso));

            SolicitudCotizacionDTO solicitud = new SolicitudCotizacionDTO();
            solicitud.setLineas(List.of(new LineaCotizacionClienteDTO("CURSO", 3, 1, null)));

            assertThatThrownBy(() -> servicio.solicitarCotizacion(CORREO, solicitud))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Línea 1: el curso seleccionado no está disponible.");

            verifyNoInteractions(cotizacionService);
        }
    }
}
