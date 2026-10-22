package sv.edu.udb.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.dto.ConceptoPagoDTO;
import sv.edu.udb.dto.PagoRequestDTO;
import sv.edu.udb.dto.PagoRespuestaDTO;
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


@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService - registro y cambios de estado de pagos")
class PagoServiceTest {


    @Mock
    private PagoRepository pagoRepositorio;


    @Mock
    private EstadoPagoRepository estadoPagoRepositorio;


    @Mock
    private MetodoPagoRepository metodoPagoRepositorio;


    @Mock
    private ClienteRepository clienteRepositorio;


    @Mock
    private CotizacionRepository cotizacionRepositorio;


    @Mock
    private ConceptoPagoService conceptoPagoService;


    @InjectMocks
    private PagoService pagoService;


    private Cliente cliente;
    private MetodoPago metodoPago;
    private Cotizacion cotizacion;


    @BeforeEach
    void prepararDatos() {
        cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setNombre("Empresa Demo");


        metodoPago = new MetodoPago();
        metodoPago.setIdMetodoPago(3);
        metodoPago.setNombre("Transferencia");


        cotizacion = new Cotizacion();
        cotizacion.setIdCotizacion(20);
        cotizacion.setTotal(new BigDecimal("339.00"));
    }


    private EstadoPago estado(String nombre) {
        EstadoPago estado = new EstadoPago();
        estado.setNombre(nombre);
        return estado;
    }


    private Pago pagoEnEstado(String nombreEstado, String monto) {
        Pago pago = new Pago();
        pago.setIdPago(50);
        pago.setFecha(LocalDate.of(2026, 10, 15));
        pago.setMonto(new BigDecimal(monto));
        pago.setCliente(cliente);
        pago.setMetodoPago(metodoPago);
        pago.setEstadoPago(estado(nombreEstado));
        return pago;
    }


    private Pago pagoDeCotizacion(String nombreEstado, String monto) {
        Pago pago = pagoEnEstado(nombreEstado, monto);
        pago.setTipoPago(TipoPago.COTIZACION);
        pago.setCotizacion(cotizacion);
        return pago;
    }


    private ConceptoPagoDTO concepto(TipoPago tipo, Integer id, Integer idCliente,
                                     String total, String pagado, boolean vigente) {
        BigDecimal totalConcepto = new BigDecimal(total);
        BigDecimal pagadoConcepto = new BigDecimal(pagado);
        return new ConceptoPagoDTO(tipo, tipo.getEtiqueta(), id, idCliente, "Descripción",
                totalConcepto, pagadoConcepto, totalConcepto.subtract(pagadoConcepto), vigente);
    }


    private PagoRequestDTO solicitudPago(TipoPago tipo, Integer idReferencia, String monto) {
        PagoRequestDTO datos = new PagoRequestDTO();
        datos.setIdCliente(1);
        datos.setIdMetodoPago(3);
        datos.setTipoPago(tipo);
        datos.setIdReferencia(idReferencia);
        datos.setFecha(LocalDate.of(2026, 10, 15));
        datos.setMonto(new BigDecimal(monto));
        return datos;
    }


    private void prepararClienteYMetodo() {
        when(clienteRepositorio.findById(1)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepositorio.findById(3)).thenReturn(Optional.of(metodoPago));
    }


    private void prepararCambioDeEstado(Pago pago, String nuevoEstado) {
        when(pagoRepositorio.findById(50)).thenReturn(Optional.of(pago));
        when(estadoPagoRepositorio.findByNombreIgnoreCase(nuevoEstado))
                .thenReturn(Optional.of(estado(nuevoEstado)));
        when(pagoRepositorio.save(pago)).thenReturn(pago);
    }


    @Test
    @DisplayName("Un pago nuevo siempre queda en estado Pendiente")
    void registrarPagoQuedaPendiente() {
        // Arrange
        PagoRequestDTO datos = solicitudPago(TipoPago.INSCRIPCION, 4, "150.00");


        prepararClienteYMetodo();
        when(conceptoPagoService.obtener(TipoPago.INSCRIPCION, 4, null))
                .thenReturn(concepto(TipoPago.INSCRIPCION, 4, 1, "300.00", "0.00", true));
        when(estadoPagoRepositorio.findByNombreIgnoreCase("Pendiente"))
                .thenReturn(Optional.of(estado("Pendiente")));
        when(pagoRepositorio.saveAndFlush(any(Pago.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));


        // Act
        PagoRespuestaDTO respuesta = pagoService.registrarPago(datos);


        // Assert
        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepositorio).saveAndFlush(captor.capture());
        Pago guardado = captor.getValue();
        assertThat(guardado.getEstadoPago().getNombre()).isEqualTo("Pendiente");
        assertThat(guardado.getIdInscripcion()).isEqualTo(4);
        assertThat(guardado.getCotizacion()).isNull();
        assertThat(respuesta.estado()).isEqualTo("Pendiente");
        assertThat(respuesta.tipoPago()).isEqualTo(TipoPago.INSCRIPCION);
        assertThat(guardado.getNumeroReferenciaExterna()).isNull();
    }


    @Test
    @DisplayName("Un pago por exactamente el saldo pendiente se acepta")
    void registrarPagoPorElSaldoExacto() {
        // Arrange
        PagoRequestDTO datos = solicitudPago(TipoPago.ALQUILER, 8, "100.00");


        prepararClienteYMetodo();
        when(conceptoPagoService.obtener(TipoPago.ALQUILER, 8, null))
                .thenReturn(concepto(TipoPago.ALQUILER, 8, 1, "400.00", "300.00", true));
        when(estadoPagoRepositorio.findByNombreIgnoreCase("Pendiente"))
                .thenReturn(Optional.of(estado("Pendiente")));
        when(pagoRepositorio.saveAndFlush(any(Pago.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));


        // Act
        PagoRespuestaDTO respuesta = pagoService.registrarPago(datos);


        // Assert
        assertThat(respuesta.idReferencia()).isEqualTo(8);
        assertThat(respuesta.estado()).isEqualTo("Pendiente");
    }


    @Test
    @DisplayName("Se rechaza pagar un concepto que pertenece a otro cliente")
    void registrarPagoDeConceptoDeOtroCliente() {
        // Arrange
        PagoRequestDTO datos = solicitudPago(TipoPago.COTIZACION, 20, "100.00");


        prepararClienteYMetodo();
        when(conceptoPagoService.obtener(TipoPago.COTIZACION, 20, null))
                .thenReturn(concepto(TipoPago.COTIZACION, 20, 2, "339.00", "0.00", true));


        // Act + Assert
        assertThatThrownBy(() -> pagoService.registrarPago(datos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cotización #20 no pertenece al cliente seleccionado.");
        verify(pagoRepositorio, never()).saveAndFlush(any());
    }


    @Test
    @DisplayName("Se rechaza un monto mayor al saldo pendiente del concepto")
    void registrarPagoMayorAlSaldo() {
        // Arrange
        PagoRequestDTO datos = solicitudPago(TipoPago.COTIZACION, 20, "200.00");


        prepararClienteYMetodo();
        when(conceptoPagoService.obtener(TipoPago.COTIZACION, 20, null))
                .thenReturn(concepto(TipoPago.COTIZACION, 20, 1, "339.00", "200.00", true));


        // Act + Assert
        assertThatThrownBy(() -> pagoService.registrarPago(datos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El monto (200.00)superaelsaldopendientedeCotización#20(139.00).");
        verify(pagoRepositorio, never()).saveAndFlush(any());
    }


    @Test
    @DisplayName("Se rechaza un concepto ya pagado por completo")
    void registrarPagoDeConceptoSinSaldo() {
        // Arrange
        PagoRequestDTO datos = solicitudPago(TipoPago.CATERING, 6, "10.00");


        prepararClienteYMetodo();
        when(conceptoPagoService.obtener(TipoPago.CATERING, 6, null))
                .thenReturn(concepto(TipoPago.CATERING, 6, 1, "250.00", "250.00", true));


        // Act + Assert
        assertThatThrownBy(() -> pagoService.registrarPago(datos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está pagado por completo");
        verify(pagoRepositorio, never()).saveAndFlush(any());
    }


    @Test
    @DisplayName("Se rechaza pagar una cotización que no está Aprobada")
    void registrarPagoDeCotizacionNoAprobada() {
        // Arrange
        PagoRequestDTO datos = solicitudPago(TipoPago.COTIZACION, 20, "100.00");


        prepararClienteYMetodo();
        when(conceptoPagoService.obtener(TipoPago.COTIZACION, 20, null))
                .thenReturn(concepto(TipoPago.COTIZACION, 20, 1, "339.00", "0.00", false));


        // Act + Assert
        assertThatThrownBy(() -> pagoService.registrarPago(datos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cotizaciones aprobadas");
        verify(pagoRepositorio, never()).saveAndFlush(any());
    }


    @Test
    @DisplayName("Al actualizar no se puede cambiar el concepto del pago")
    void actualizarPagoNoCambiaElConcepto() {
        // Arrange
        Pago pago = pagoDeCotizacion("Pendiente", "100.00");
        when(pagoRepositorio.findById(50)).thenReturn(Optional.of(pago));
        PagoRequestDTO datos = solicitudPago(TipoPago.ALQUILER, 8, "100.00");


        // Act + Assert
        assertThatThrownBy(() -> pagoService.actualizarPago(50, datos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no se pueden cambiar");
        verify(pagoRepositorio, never()).saveAndFlush(any());
    }


    @Test
    @DisplayName("Al actualizar, el saldo no cuenta el propio pago: se puede subir el monto hasta cubrir el total")
    void actualizarPagoExcluyeElPropioPagoDelSaldo() {
        // Arrange
        Pago pago = pagoDeCotizacion("Pendiente", "100.00");
        when(pagoRepositorio.findById(50)).thenReturn(Optional.of(pago));
        when(metodoPagoRepositorio.findById(3)).thenReturn(Optional.of(metodoPago));
        when(conceptoPagoService.obtener(TipoPago.COTIZACION, 20, 50))
                .thenReturn(concepto(TipoPago.COTIZACION, 20, 1, "339.00", "0.00", true));
        when(cotizacionRepositorio.findById(20)).thenReturn(Optional.of(cotizacion));
        when(pagoRepositorio.saveAndFlush(pago)).thenReturn(pago);


        // Act
        PagoRespuestaDTO respuesta = pagoService.actualizarPago(50,
                solicitudPago(TipoPago.COTIZACION, 20, "339.00"));


        // Assert
        assertThat(respuesta.monto()).isEqualByComparingTo("339.00");
        verify(conceptoPagoService).obtener(TipoPago.COTIZACION, 20, 50);
    }


    @Test
    @DisplayName("Validar un pago de cotización que cubre el total lo deja Pagado")
    void validarPagoQueCubreElTotal() {
        // Arrange
        Pago pago = pagoDeCotizacion("Pendiente", "339.00");
        prepararCambioDeEstado(pago, "Pagado");
        when(pagoRepositorio.findByCotizacion_IdCotizacion(20)).thenReturn(List.of(pago));


        // Act
        PagoRespuestaDTO respuesta = pagoService.validarPago(50);


        // Assert
        assertThat(respuesta.estado()).isEqualTo("Pagado");
        verify(estadoPagoRepositorio).findByNombreIgnoreCase("Pagado");
        verify(pagoRepositorio).save(pago);
    }


    @Test
    @DisplayName("Validar suma todos los pagos de la cotización: dos abonos que cubren el total -> Pagado")
    void validarPagoSumaAbonosAnteriores() {
        // Arrange
        Pago anterior = pagoDeCotizacion("Parcial", "200.00");
        Pago pago = pagoDeCotizacion("Pendiente", "139.00");
        prepararCambioDeEstado(pago, "Pagado");
        when(pagoRepositorio.findByCotizacion_IdCotizacion(20))
                .thenReturn(List.of(anterior, pago));


        // Act
        PagoRespuestaDTO respuesta = pagoService.validarPago(50);


        // Assert
        assertThat(respuesta.estado()).isEqualTo("Pagado");
    }


    @Test
    @DisplayName("Validar un pago de cotización que no cubre el total lo deja Parcial")
    void validarPagoQueNoCubreElTotal() {
        // Arrange
        Pago pago = pagoDeCotizacion("Pendiente", "100.00");
        prepararCambioDeEstado(pago, "Parcial");
        when(pagoRepositorio.findByCotizacion_IdCotizacion(20)).thenReturn(List.of(pago));


        // Act
        PagoRespuestaDTO respuesta = pagoService.validarPago(50);


        // Assert
        assertThat(respuesta.estado()).isEqualTo("Parcial");
        verify(estadoPagoRepositorio, never()).findByNombreIgnoreCase("Pagado");
    }


    @Test
    @DisplayName("Validar un pago sin cotización lo deja Pagado sin consultar otros pagos")
    void validarPagoSinCotizacion() {
        // Arrange
        Pago pago = pagoEnEstado("Pendiente", "150.00");
        pago.setTipoPago(TipoPago.INSCRIPCION);
        pago.setIdInscripcion(4);
        prepararCambioDeEstado(pago, "Pagado");


        // Act
        PagoRespuestaDTO respuesta = pagoService.validarPago(50);


        // Assert
        assertThat(respuesta.estado()).isEqualTo("Pagado");
        verify(pagoRepositorio, never()).findByCotizacion_IdCotizacion(any());
    }


    @Test
    @DisplayName("No se puede validar un pago que ya está Pagado")
    void validarPagoYaPagadoSeRechaza() {
        // Arrange
        when(pagoRepositorio.findById(50))
                .thenReturn(Optional.of(pagoDeCotizacion("Pagado", "339.00")));


        // Act + Assert
        assertThatThrownBy(() -> pagoService.validarPago(50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está validado");
        verify(pagoRepositorio, never()).save(any());
    }


    @Test
    @DisplayName("Marcar como parcial un pago Pendiente lo deja Parcial")
    void marcarComoParcialPagoPendiente() {
        // Arrange
        Pago pago = pagoDeCotizacion("Pendiente", "100.00");
        prepararCambioDeEstado(pago, "Parcial");


        // Act
        PagoRespuestaDTO respuesta = pagoService.marcarComoParcial(50);


        // Assert
        assertThat(respuesta.estado()).isEqualTo("Parcial");
        verify(pagoRepositorio).save(pago);
    }


    @ParameterizedTest(name = "No se puede marcar como parcial un pago \"{0}\"")
    @ValueSource(strings = {"Parcial", "Pagado"})
    @DisplayName("Marcar como parcial se rechaza si ya está Parcial o Pagado")
    void marcarComoParcialSeRechaza(String estadoActual) {
        // Arrange
        when(pagoRepositorio.findById(50))
                .thenReturn(Optional.of(pagoDeCotizacion(estadoActual, "100.00")));


        // Act + Assert
        assertThatThrownBy(() -> pagoService.marcarComoParcial(50))
                .isInstanceOf(IllegalArgumentException.class);
        verify(pagoRepositorio, never()).save(any());
        verify(estadoPagoRepositorio, never()).findByNombreIgnoreCase(any());
    }
}




