package sv.edu.udb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sv.edu.udb.dto.PerfilClienteDTO;
import sv.edu.udb.exception.CampoInvalidoException;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Rol;
import sv.edu.udb.model.Usuario;
import sv.edu.udb.repository.ClienteRepository;

/** Pruebas de "Mis datos" del portal del cliente. */
@ExtendWith(MockitoExtension.class)
class PerfilClienteServiceTest {

    private static final String CORREO = "ana@prueba.local";

    @Mock private ClienteRepository clienteRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private PasswordEncoder passwordEncoder;

    private PerfilClienteService servicio;
    private Cliente cliente;

    @BeforeEach
    void setUp() {

        servicio = new PerfilClienteService(
                clienteRepository,
                usuarioService,
                passwordEncoder
        );

        Usuario usuario = new Usuario(5, "Ana López", CORREO, "hash-bcrypt",
                new Rol(3, "CLIENTE", null));

        cliente = new Cliente(7, null, "Ana López", null, CORREO, null, null);
        cliente.setUsuario(usuario);
    }

    private PerfilClienteDTO datos(String dui, String correo, String passwordActual) {
        return new PerfilClienteDTO("Ana López", dui, null, correo,
                "2222-3333", "San Salvador", passwordActual);
    }

    @Test
    @DisplayName("Perfil incompleto: falta DUI/NIT, teléfono o dirección (empresa es opcional)")
    void perfilIncompleto() {

        assertThat(servicio.estaIncompleto(cliente)).isTrue();

        cliente.setDui("01234567-8");
        cliente.setTelefono("2222-3333");
        assertThat(servicio.estaIncompleto(cliente)).isTrue();

        cliente.setDireccion("San Salvador");
        assertThat(servicio.estaIncompleto(cliente)).isFalse();

        cliente.setTelefono("   ");
        assertThat(servicio.estaIncompleto(cliente)).isTrue();
    }

    @ParameterizedTest(name = "DUI/NIT inválido \"{0}\"")
    @ValueSource(strings = {"12345678", "1234567-8", "01234567-89", "0123456A-8",
            "0614-120590-1011", "0614120590101-1", "DUI 01234567-8"})
    @DisplayName("DUI/NIT con formato inválido: se rechaza en el campo dui sin tocar la BD")
    void duiInvalido(String dui) {

        when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                .thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> servicio.actualizar(CORREO, datos(dui, CORREO, null)))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("00000000-0")
                .extracting("campo").isEqualTo("dui");

        verify(clienteRepository, never()).findByDui(any());
        verify(clienteRepository, never()).save(any());
        verifyNoInteractions(usuarioService);
    }

    @ParameterizedTest(name = "Teléfono inválido \"{0}\"")
    @ValueSource(strings = {"2222333", "2222-33334", "5222-3333", "+502 7000-0000",
            "7000 0000", "+503-7000-0000", "teléfono"})
    @DisplayName("Teléfono con formato inválido: se rechaza en el campo telefono sin tocar la BD")
    void telefonoInvalido(String telefono) {

        when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                .thenReturn(Optional.of(cliente));

        PerfilClienteDTO datos = datos("01234567-8", CORREO, null);
        datos.setTelefono(telefono);

        assertThatThrownBy(() -> servicio.actualizar(CORREO, datos))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("8 dígitos")
                .extracting("campo").isEqualTo("telefono");

        verify(clienteRepository, never()).save(any());
        verifyNoInteractions(usuarioService);
    }

    @ParameterizedTest(name = "DUI/NIT \"{0}\" y teléfono \"{1}\" se guardan como \"{2}\"")
    @CsvSource({
            "01234567-8,        2222-3333,       2222-3333",
            "0614-120590-101-1, 7000-0009,       7000-0009",
            "01234567-8,        +5036000-1234,   +503 6000-1234",
            "01234567-8,        +503 7123-4567,  +503 7123-4567"
    })
    @DisplayName("DUI/NIT y teléfono válidos se guardan (el prefijo +503 se normaliza)")
    void duiYTelefonoValidos(String dui, String telefono, String telefonoGuardado) {

        when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                .thenReturn(Optional.of(cliente));
        when(clienteRepository.findByDui(dui)).thenReturn(Optional.empty());

        PerfilClienteDTO datos = datos(dui, CORREO, null);
        datos.setTelefono(telefono);

        servicio.actualizar(CORREO, datos);

        assertThat(cliente.getDui()).isEqualTo(dui);
        assertThat(cliente.getTelefono()).isEqualTo(telefonoGuardado);
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("DUI/NIT duplicado en otro cliente: se rechaza en el campo dui y no se guarda")
    void duiDuplicado() {

        when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                .thenReturn(Optional.of(cliente));

        Cliente otro = new Cliente(9, "01234567-8", "Otro", null, "otro@x.com", null, null);
        when(clienteRepository.findByDui("01234567-8")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> servicio.actualizar(CORREO, datos("01234567-8", CORREO, null)))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("01234567-8")
                .extracting("campo").isEqualTo("dui");

        verify(clienteRepository, never()).save(any());
        verifyNoInteractions(usuarioService);
    }

    @Test
    @DisplayName("Su propio DUI/NIT no cuenta como duplicado")
    void mismoDuiPropio() {

        cliente.setDui("01234567-8");
        when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                .thenReturn(Optional.of(cliente));
        when(clienteRepository.findByDui("01234567-8")).thenReturn(Optional.of(cliente));

        boolean cambioCorreo = servicio.actualizar(CORREO, datos("01234567-8", CORREO, null));

        assertThat(cambioCorreo).isFalse();
        assertThat(cliente.getTelefono()).isEqualTo("2222-3333");
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Cambio de correo con contraseña incorrecta: se rechaza en passwordActual y no se guarda")
    void cambioCorreoConPasswordIncorrecta() {

        when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                .thenReturn(Optional.of(cliente));
        when(clienteRepository.findByDui("01234567-8")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("Incorrecta@1", "hash-bcrypt")).thenReturn(false);

        assertThatThrownBy(() -> servicio.actualizar(CORREO,
                datos("01234567-8", "nuevo@prueba.local", "Incorrecta@1")))
                .isInstanceOf(CampoInvalidoException.class)
                .extracting("campo").isEqualTo("passwordActual");

        verify(clienteRepository, never()).save(any());
        verifyNoInteractions(usuarioService);
    }

    @Test
    @DisplayName("Cambio de correo sin escribir la contraseña: también se rechaza")
    void cambioCorreoSinPassword() {

        when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                .thenReturn(Optional.of(cliente));
        when(clienteRepository.findByDui("01234567-8")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.actualizar(CORREO,
                datos("01234567-8", "nuevo@prueba.local", "")))
                .isInstanceOf(CampoInvalidoException.class)
                .extracting("campo").isEqualTo("passwordActual");

        verifyNoInteractions(passwordEncoder, usuarioService);
    }

    @Test
    @DisplayName("Cambio de correo con contraseña correcta: actualiza usuario y cliente y avisa que cambió")
    void cambioCorreoCorrecto() {

        when(clienteRepository.findByUsuario_EmailIgnoreCase(CORREO))
                .thenReturn(Optional.of(cliente));
        when(clienteRepository.findByDui("01234567-8")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("Correcta@1", "hash-bcrypt")).thenReturn(true);
        when(usuarioService.actualizarCuentaCliente(any(), any(), any(), any()))
                .thenAnswer(invocacion -> {
                    Usuario usuario = invocacion.getArgument(0);
                    usuario.setEmail(invocacion.getArgument(2));
                    return usuario;
                });

        boolean cambioCorreo = servicio.actualizar(CORREO,
                datos("01234567-8", " Nuevo@Prueba.local ", "Correcta@1"));

        assertThat(cambioCorreo).isTrue();
        assertThat(cliente.getCorreo()).isEqualTo("nuevo@prueba.local");
        assertThat(cliente.getDui()).isEqualTo("01234567-8");
        verify(usuarioService).actualizarCuentaCliente(
                cliente.getUsuario(), "Ana López", "nuevo@prueba.local", null);
        verify(clienteRepository).save(cliente);
    }
}
