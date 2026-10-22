package sv.edu.udb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sv.edu.udb.dto.RegistroClienteDTO;
import sv.edu.udb.exception.CorreoEnUsoException;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Rol;
import sv.edu.udb.model.Usuario;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.RolRepository;
import sv.edu.udb.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class RegistroClienteServiceTest {

    private static final String PASSWORD_VALIDA = "Segura@2026";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClienteRepository clienteRepository;

    private RegistroClienteService registroClienteService;

    @BeforeEach
    void setUp() {

        UsuarioService usuarioService = new UsuarioService(
                usuarioRepository,
                rolRepository,
                passwordEncoder
        );

        ClienteService clienteService = new ClienteService(
                clienteRepository,
                usuarioService
        );

        registroClienteService = new RegistroClienteService(
                usuarioService,
                clienteService,
                usuarioRepository
        );
    }

    @Test
    @DisplayName("Registro exitoso: crea usuario CLIENTE activo y cliente vinculado")
    void registrarCreaUsuarioClienteYClienteVinculado() {

        prepararCorreoLibre("ana@correo.com");
        prepararGuardadoUsuario();

        when(clienteRepository
                .findFirstByCorreoIgnoreCaseAndUsuarioIsNullOrderByIdClienteAsc(
                        "ana@correo.com"))
                .thenReturn(Optional.empty());

        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocacion -> {
                    Cliente cliente = invocacion.getArgument(0);
                    cliente.setIdCliente(20);
                    return cliente;
                });

        Cliente cliente = registroClienteService.registrar(
                datos("  Ana López ", "  Ana@Correo.COM ",
                        PASSWORD_VALIDA, PASSWORD_VALIDA)
        );

        Usuario usuario = cliente.getUsuario();

        assertThat(cliente.getIdCliente()).isEqualTo(20);
        assertThat(cliente.getNombre()).isEqualTo("Ana López");
        assertThat(cliente.getCorreo()).isEqualTo("ana@correo.com");

        assertThat(usuario).isNotNull();
        assertThat(usuario.getId()).isEqualTo(10);
        assertThat(usuario.getEmail()).isEqualTo("ana@correo.com");
        assertThat(usuario.getRol().getNombre()).isEqualTo("CLIENTE");
        assertThat(usuario.getEstado()).isTrue();
        assertThat(usuario.getPassword()).isEqualTo("hash-bcrypt");

        verify(passwordEncoder).encode(PASSWORD_VALIDA);
        verify(rolRepository).findByNombre("CLIENTE");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Correo duplicado: lanza CorreoEnUsoException y no guarda nada")
    void registrarConCorreoDuplicadoLanzaExcepcion() {

        when(usuarioRepository.existsByEmail("ana@correo.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> registroClienteService.registrar(
                datos("Ana", "ana@correo.com",
                        PASSWORD_VALIDA, PASSWORD_VALIDA)))
                .isInstanceOf(CorreoEnUsoException.class)
                .hasMessage("Ya existe una cuenta registrada "
                        + "con ese correo electrónico");

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(clienteRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Contraseñas distintas: se rechaza antes de tocar la BD")
    void registrarConContrasenasDistintasLanzaExcepcion() {

        assertThatThrownBy(() -> registroClienteService.registrar(
                datos("Ana", "ana@correo.com",
                        PASSWORD_VALIDA, "Otra@2026x")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Las contraseñas no coinciden");

        verifyNoInteractions(
                usuarioRepository,
                clienteRepository,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("Contraseña débil: se aplica la política existente de UsuarioService")
    void registrarConContrasenaDebilLanzaExcepcion() {

        prepararCorreoLibre("ana@correo.com");

        assertThatThrownBy(() -> registroClienteService.registrar(
                datos("Ana", "ana@correo.com",
                        "debil123", "debil123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mínimo 8 caracteres");

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(clienteRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Cliente existente sin cuenta: se vincula en lugar de duplicarlo")
    void registrarVinculaClienteExistenteSinCuenta() {

        prepararCorreoLibre("ana@correo.com");
        prepararGuardadoUsuario();

        Cliente existente = new Cliente(
                5,
                "01234567-8",
                "Ana López (Recepción)",
                "Empresa S.A.",
                "Ana@Correo.com",
                "2222-3333",
                "San Salvador"
        );

        when(clienteRepository
                .findFirstByCorreoIgnoreCaseAndUsuarioIsNullOrderByIdClienteAsc(
                        "ana@correo.com"))
                .thenReturn(Optional.of(existente));

        when(clienteRepository.save(existente))
                .thenReturn(existente);

        Cliente cliente = registroClienteService.registrar(
                datos("Ana López", "ana@correo.com",
                        PASSWORD_VALIDA, PASSWORD_VALIDA)
        );

        assertThat(cliente).isSameAs(existente);
        assertThat(cliente.getIdCliente()).isEqualTo(5);
        assertThat(cliente.getDui()).isEqualTo("01234567-8");
        assertThat(cliente.getNombre()).isEqualTo("Ana López (Recepción)");
        assertThat(cliente.getUsuario()).isNotNull();
        assertThat(cliente.getUsuario().getRol().getNombre())
                .isEqualTo("CLIENTE");

        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(clienteRepository).save(existente);
    }

    private void prepararCorreoLibre(String correo) {

        when(usuarioRepository.existsByEmail(correo))
                .thenReturn(false);

        when(usuarioRepository.findByEmail(correo))
                .thenReturn(Optional.empty());
    }

    private void prepararGuardadoUsuario() {

        when(rolRepository.findByNombre("CLIENTE"))
                .thenReturn(Optional.of(
                        new Rol(4, "CLIENTE", "Cliente del CFC")
                ));

        when(passwordEncoder.encode(PASSWORD_VALIDA))
                .thenReturn("hash-bcrypt");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacion -> {
                    Usuario usuario = invocacion.getArgument(0);
                    usuario.setId(10);
                    return usuario;
                });
    }

    private RegistroClienteDTO datos(
            String nombre,
            String correo,
            String password,
            String confirmarPassword) {

        return new RegistroClienteDTO(
                nombre,
                correo,
                password,
                confirmarPassword
        );
    }
}
