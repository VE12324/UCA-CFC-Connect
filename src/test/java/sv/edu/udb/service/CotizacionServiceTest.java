package sv.edu.udb.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.dto.CotizacionRequestDTO;
import sv.edu.udb.dto.CotizacionRespuestaDTO;
import sv.edu.udb.dto.DetalleCotizacionItemDTO;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Cotizacion;
import sv.edu.udb.model.DetalleCotizacion;
import sv.edu.udb.model.EstadoCotizacion;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.CotizacionRepository;
import sv.edu.udb.repository.DetalleCotizacionRepository;
import sv.edu.udb.repository.EstadoCotizacionRepository;


@ExtendWith(MockitoExtension.class)
@DisplayName("CotizacionService - totales, estados y reglas de edición")
class CotizacionServiceTest {


    @Mock
    private CotizacionRepository cotizacionRepositorio;


    @Mock
    private DetalleCotizacionRepository detalleCotizacionRepositorio;


    @Mock
    private EstadoCotizacionRepository estadoCotizacionRepositorio;


    @Mock
    private ClienteRepository clienteRepositorio;


    @InjectMocks
    private CotizacionService cotizacionService;


    private Cliente cliente;


    @BeforeEach
    void prepararDatos() {
        cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setNombre("Empresa Demo");
    }


    private EstadoCotizacion estado(String nombre) {
        EstadoCotizacion estado = new EstadoCotizacion();
        estado.setNombre(nombre);
        return estado;
    }


    private Cotizacion cotizacionEnEstado(String nombreEstado) {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setIdCotizacion(7);
        cotizacion.setFecha(LocalDate.of(2026, 10, 1));
        cotizacion.setCliente(cliente);
        cotizacion.setEstadoCotizacion(estado(nombreEstado));
        return cotizacion;
    }


    private DetalleCotizacionItemDTO detalle(String descripcion, int cantidad, String precio) {
        DetalleCotizacionItemDTO item = new DetalleCotizacionItemDTO();
        item.setDescripcion(descripcion);
        item.setCantidad(cantidad);
        item.setPrecio(new BigDecimal(precio));
        return item;
    }


    private CotizacionRequestDTO solicitud(List<DetalleCotizacionItemDTO> detalles) {
        CotizacionRequestDTO datos = new CotizacionRequestDTO();
        datos.setIdCliente(1);
        datos.setFecha(LocalDate.of(2026, 10, 1));
        datos.setDetalles(detalles);
        return datos;
    }


    @Test
    @DisplayName("El total es el subtotal por 1.13 (IVA 13 %) y la cotización nace Pendiente")
    void registrarCalculaSubtotalYTotalConIva() {
        // Arrange
        CotizacionRequestDTO datos = solicitud(List.of(
                detalle("Alquiler de auditorio", 2, "100.00"),
                detalle("Refrigerio", 10, "5.50")
        ));
        when(clienteRepositorio.findById(1)).thenReturn(Optional.of(cliente));
        when(estadoCotizacionRepositorio.findByNombreIgnoreCase("Pendiente"))
                .thenReturn(Optional.of(estado("Pendiente")));
        when(cotizacionRepositorio.save(any(Cotizacion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(detalleCotizacionRepositorio.findByCotizacion_IdCotizacion(any()))
                .thenReturn(new ArrayList<DetalleCotizacion>());


        // Act
        CotizacionRespuestaDTO respuesta = cotizacionService.registrarCotizacion(datos);


        // Assert: 2 x 100.00 + 10 x 5.50 = 255.00; 255.00 x 1.13 = 288.15
        assertThat(respuesta.subtotal()).isEqualByComparingTo("255.00");
        assertThat(respuesta.total()).isEqualByComparingTo("288.15");
        assertThat(respuesta.estado()).isEqualTo("Pendiente");
        verify(detalleCotizacionRepositorio, times(2)).save(any(DetalleCotizacion.class));
    }


    @Test
    @DisplayName("Redondea el total con IVA a 2 decimales (HALF_UP)")
    void registrarRedondeaTotal() {
        // Arrange: 1 x 10.01 = 10.01; 10.01 x 1.13 = 11.3113 -> 11.31
        CotizacionRequestDTO datos = solicitud(List.of(detalle("Servicio", 1, "10.01")));
        when(clienteRepositorio.findById(1)).thenReturn(Optional.of(cliente));
        when(estadoCotizacionRepositorio.findByNombreIgnoreCase("Pendiente"))
                .thenReturn(Optional.of(estado("Pendiente")));
        when(cotizacionRepositorio.save(any(Cotizacion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(detalleCotizacionRepositorio.findByCotizacion_IdCotizacion(any()))
                .thenReturn(new ArrayList<DetalleCotizacion>());


        // Act
        CotizacionRespuestaDTO respuesta = cotizacionService.registrarCotizacion(datos);


        // Assert
        assertThat(respuesta.total()).isEqualByComparingTo("11.31");
    }


    @Test
    @DisplayName("Exige al menos un detalle: lista vacía se rechaza sin tocar repositorios")
    void registrarSinDetallesSeRechaza() {
        // Arrange
        CotizacionRequestDTO datos = solicitud(List.of());


        // Act + Assert
        assertThatThrownBy(() -> cotizacionService.registrarCotizacion(datos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un detalle");
        verifyNoInteractions(cotizacionRepositorio, detalleCotizacionRepositorio);
    }


    @Test
    @DisplayName("Exige al menos un detalle: lista nula se rechaza")
    void registrarConDetallesNulosSeRechaza() {
        // Arrange
        CotizacionRequestDTO datos = solicitud(null);


        // Act + Assert
        assertThatThrownBy(() -> cotizacionService.registrarCotizacion(datos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un detalle");
        verify(cotizacionRepositorio, never()).save(any());
    }


    @Test
    @DisplayName("No se puede editar una cotización Aprobada")
    void actualizarCotizacionAprobadaSeRechaza() {
        // Arrange
        when(cotizacionRepositorio.findById(7))
                .thenReturn(Optional.of(cotizacionEnEstado("Aprobada")));
        CotizacionRequestDTO datos = solicitud(List.of(detalle("Servicio", 1, "10.00")));


        // Act + Assert
        assertThatThrownBy(() -> cotizacionService.actualizarCotizacion(7, datos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya aprobada");
        verify(cotizacionRepositorio, never()).save(any());
        verify(detalleCotizacionRepositorio, never()).deleteByCotizacion_IdCotizacion(anyInt());
    }


    @Test
    @DisplayName("No se puede eliminar una cotización Aprobada")
    void eliminarCotizacionAprobadaSeRechaza() {
        // Arrange
        when(cotizacionRepositorio.findById(7))
                .thenReturn(Optional.of(cotizacionEnEstado("Aprobada")));


        // Act + Assert
        assertThatThrownBy(() -> cotizacionService.eliminarCotizacion(7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya aprobada");
        verify(cotizacionRepositorio, never()).delete(any());
        verify(detalleCotizacionRepositorio, never()).deleteByCotizacion_IdCotizacion(anyInt());
    }


    @Test
    @DisplayName("Se puede eliminar una cotización Pendiente (borra primero sus detalles)")
    void eliminarCotizacionPendiente() {
        // Arrange
        Cotizacion cotizacion = cotizacionEnEstado("Pendiente");
        when(cotizacionRepositorio.findById(7)).thenReturn(Optional.of(cotizacion));


        // Act
        cotizacionService.eliminarCotizacion(7);


        // Assert
        verify(detalleCotizacionRepositorio).deleteByCotizacion_IdCotizacion(7);
        verify(cotizacionRepositorio).delete(cotizacion);
    }


    @ParameterizedTest(name = "Aprueba una cotización en estado \"{0}\"")
    @ValueSource(strings = {"Pendiente", "En proceso"})
    @DisplayName("Aprueba cotizaciones Pendiente o En proceso")
    void aprobarPendienteOEnProceso(String estadoInicial) {
        // Arrange
        Cotizacion cotizacion = cotizacionEnEstado(estadoInicial);
        when(cotizacionRepositorio.findById(7)).thenReturn(Optional.of(cotizacion));
        when(estadoCotizacionRepositorio.findByNombreIgnoreCase("Aprobada"))
                .thenReturn(Optional.of(estado("Aprobada")));
        when(cotizacionRepositorio.save(cotizacion)).thenReturn(cotizacion);
        when(detalleCotizacionRepositorio.findByCotizacion_IdCotizacion(7))
                .thenReturn(new ArrayList<DetalleCotizacion>());


        // Act
        CotizacionRespuestaDTO respuesta = cotizacionService.aprobarCotizacion(7);


        // Assert
        assertThat(respuesta.estado()).isEqualTo("Aprobada");
        verify(cotizacionRepositorio).save(cotizacion);
    }


    @ParameterizedTest(name = "Rechaza una cotización en estado \"{0}\"")
    @ValueSource(strings = {"Pendiente", "En proceso"})
    @DisplayName("Rechaza cotizaciones Pendiente o En proceso")
    void rechazarPendienteOEnProceso(String estadoInicial) {
        // Arrange
        Cotizacion cotizacion = cotizacionEnEstado(estadoInicial);
        when(cotizacionRepositorio.findById(7)).thenReturn(Optional.of(cotizacion));
        when(estadoCotizacionRepositorio.findByNombreIgnoreCase("Rechazada"))
                .thenReturn(Optional.of(estado("Rechazada")));
        when(cotizacionRepositorio.save(cotizacion)).thenReturn(cotizacion);
        when(detalleCotizacionRepositorio.findByCotizacion_IdCotizacion(7))
                .thenReturn(new ArrayList<DetalleCotizacion>());


        // Act
        CotizacionRespuestaDTO respuesta = cotizacionService.rechazarCotizacion(7);


        // Assert
        assertThat(respuesta.estado()).isEqualTo("Rechazada");
    }


    @ParameterizedTest(name = "No se puede aprobar una cotización \"{0}\"")
    @ValueSource(strings = {"Aprobada", "Rechazada"})
    @DisplayName("No aprueba cotizaciones ya resueltas")
    void aprobarCotizacionResueltaSeRechaza(String estadoInicial) {
        // Arrange
        when(cotizacionRepositorio.findById(7))
                .thenReturn(Optional.of(cotizacionEnEstado(estadoInicial)));


        // Act + Assert
        assertThatThrownBy(() -> cotizacionService.aprobarCotizacion(7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pendientes o en proceso");
        verify(cotizacionRepositorio, never()).save(any());
    }


    @ParameterizedTest(name = "No se puede rechazar una cotización \"{0}\"")
    @ValueSource(strings = {"Aprobada", "Rechazada"})
    @DisplayName("No rechaza cotizaciones ya resueltas")
    void rechazarCotizacionResueltaSeRechaza(String estadoInicial) {
        // Arrange
        when(cotizacionRepositorio.findById(7))
                .thenReturn(Optional.of(cotizacionEnEstado(estadoInicial)));


        // Act + Assert
        assertThatThrownBy(() -> cotizacionService.rechazarCotizacion(7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pendientes o en proceso");
        verify(cotizacionRepositorio, never()).save(any());
    }
}


