package sv.edu.udb.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.exception.EspacioOcupadoException;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.repository.AlquilerRepository;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.EspacioRepository;


@ExtendWith(MockitoExtension.class)
@DisplayName("AlquilerService - reglas de alquiler de espacios")
class AlquilerServiceTest {


    private static final LocalDateTime INICIO =
            LocalDateTime.of(2026, 11, 10, 8, 0);
    private static final LocalDateTime FIN =
            LocalDateTime.of(2026, 11, 10, 12, 0);


    @Mock
    private AlquilerRepository alquilerRepository;


    @Mock
    private ClienteRepository clienteRepository;


    @Mock
    private EspacioRepository espacioRepository;


    @InjectMocks
    private AlquilerService alquilerService;


    private Cliente cliente;
    private Espacio espacio;


    @BeforeEach
    void prepararDatos() {
        cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setNombre("Empresa Demo");


        espacio = new Espacio();
        espacio.setIdEspacio(5);
        espacio.setNombre("Auditorio");
        espacio.setCapacidad(50);
        espacio.setEstado("DISPONIBLE");
    }


    private Alquiler nuevoAlquiler(int personas) {
        Alquiler alquiler = new Alquiler();
        alquiler.setFecha(LocalDate.of(2026, 11, 10));
        alquiler.setHoraInicio(INICIO);
        alquiler.setHoraFin(FIN);
        alquiler.setCantidadPersonas(personas);
        alquiler.setEstado("");
        alquiler.setCliente(cliente);
        alquiler.setEspacio(espacio);
        return alquiler;
    }


    private void prepararClienteYEspacio() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(espacioRepository.findById(5)).thenReturn(Optional.of(espacio));
    }


    @Test
    @DisplayName("Registra el alquiler cuando el espacio está disponible y el horario libre")
    void registrarAlquilerExitoso() {
        // Arrange
        prepararClienteYEspacio();
        Alquiler alquiler = nuevoAlquiler(30);
        when(alquilerRepository.existeConflictoHorario(5, INICIO, FIN, null))
                .thenReturn(false);
        when(alquilerRepository.save(any(Alquiler.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));


        // Act
        Alquiler guardado = alquilerService.registrarAlquiler(alquiler);


        // Assert
        assertThat(guardado.getEstado()).isEqualTo("PENDIENTE");
        assertThat(guardado.getEspacio()).isSameAs(espacio);
        assertThat(guardado.getCliente()).isSameAs(cliente);
        verify(alquilerRepository).existeConflictoHorario(5, INICIO, FIN, null);
        verify(alquilerRepository).save(alquiler);
    }


    @Test
    @DisplayName("Lanza EspacioOcupadoException si el horario se traslapa con otro alquiler")
    void registrarAlquilerConSolapamiento() {
        // Arrange
        prepararClienteYEspacio();
        Alquiler alquiler = nuevoAlquiler(30);
        when(alquilerRepository.existeConflictoHorario(5, INICIO, FIN, null))
                .thenReturn(true);


        // Act + Assert
        assertThatThrownBy(() -> alquilerService.registrarAlquiler(alquiler))
                .isInstanceOf(EspacioOcupadoException.class)
                .hasMessageContaining("ya está reservado");
        verify(alquilerRepository, never()).save(any());
    }


    @Test
    @DisplayName("Rechaza el alquiler si el espacio está en mantenimiento y no guarda")
    void registrarAlquilerEspacioEnMantenimiento() {
        // Arrange
        espacio.setEstado("MANTENIMIENTO");
        prepararClienteYEspacio();
        Alquiler alquiler = nuevoAlquiler(30);


        // Act + Assert
        assertThatThrownBy(() -> alquilerService.registrarAlquiler(alquiler))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mantenimiento");
        verify(alquilerRepository, never()).save(any());
        verify(alquilerRepository, never())
                .existeConflictoHorario(anyInt(), any(), any(), any());
    }


    @Test
    @DisplayName("Rechaza el alquiler si la cantidad de personas supera la capacidad y no guarda")
    void registrarAlquilerSuperaCapacidad() {
        // Arrange
        prepararClienteYEspacio();
        Alquiler alquiler = nuevoAlquiler(51);


        // Act + Assert
        assertThatThrownBy(() -> alquilerService.registrarAlquiler(alquiler))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacidad");
        verify(alquilerRepository, never()).save(any());
    }


    @Test
    @DisplayName("Rechaza el alquiler si la hora de fin es anterior a la de inicio")
    void registrarAlquilerHoraFinAnterior() {
        // Arrange
        prepararClienteYEspacio();
        Alquiler alquiler = nuevoAlquiler(30);
        alquiler.setHoraFin(INICIO.minusHours(1));


        // Act + Assert
        assertThatThrownBy(() -> alquilerService.registrarAlquiler(alquiler))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");
        verify(alquilerRepository, never()).save(any());
    }


    @Test
    @DisplayName("Rechaza el alquiler si la hora de fin es igual a la de inicio")
    void registrarAlquilerHoraFinIgual() {
        // Arrange
        prepararClienteYEspacio();
        Alquiler alquiler = nuevoAlquiler(30);
        alquiler.setHoraFin(INICIO);


        // Act + Assert
        assertThatThrownBy(() -> alquilerService.registrarAlquiler(alquiler))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");
        verify(alquilerRepository, never()).save(any());
    }


    @Test
    @DisplayName("Al editar, el alquiler no choca consigo mismo (se excluye su propio ID)")
    void actualizarAlquilerNoChocaConsigoMismo() {
        // Arrange
        Alquiler existente = nuevoAlquiler(20);
        existente.setIdAlquiler(9);
        existente.setEstado("CONFIRMADO");
        when(alquilerRepository.findById(9)).thenReturn(Optional.of(existente));
        prepararClienteYEspacio();


        Alquiler cambios = nuevoAlquiler(40);
        cambios.setEstado("CONFIRMADO");
        when(alquilerRepository.existeConflictoHorario(5, INICIO, FIN, 9))
                .thenReturn(false);
        when(alquilerRepository.save(any(Alquiler.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));


        // Act
        Alquiler actualizado = alquilerService.actualizarAlquiler(9, cambios);


        // Assert
        assertThat(actualizado.getIdAlquiler()).isEqualTo(9);
        assertThat(actualizado.getCantidadPersonas()).isEqualTo(40);
        verify(alquilerRepository)
                .existeConflictoHorario(eq(5), eq(INICIO), eq(FIN), eq(9));
        verify(alquilerRepository).save(existente);
    }


    @Test
    @DisplayName("Un alquiler cancelado no verifica choque de horarios")
    void registrarAlquilerCanceladoNoVerificaChoque() {
        // Arrange
        prepararClienteYEspacio();
        Alquiler alquiler = nuevoAlquiler(30);
        alquiler.setEstado("cancelado");
        when(alquilerRepository.save(any(Alquiler.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));


        // Act
        Alquiler guardado = alquilerService.registrarAlquiler(alquiler);


        // Assert
        assertThat(guardado.getEstado()).isEqualTo("CANCELADO");
        verify(alquilerRepository, never())
                .existeConflictoHorario(anyInt(), any(), any(), any());
        verify(alquilerRepository).save(alquiler);
    }
}


