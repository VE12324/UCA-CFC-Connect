package sv.edu.udb.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.RegistroClienteDTO;
import sv.edu.udb.exception.CorreoEnUsoException;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Usuario;
import sv.edu.udb.repository.UsuarioRepository;


/**
 * Auto-registro público de clientes.
 *
 * Crea un Usuario con rol CLIENTE (asignado aquí, nunca leído de la
 * petición) y lo vincula a un Cliente: si ya existe un cliente sin cuenta
 * con el mismo correo (registrado por Recepción) se reutiliza; si no, se
 * crea uno nuevo. Todo ocurre en una sola transacción.
 */
@Service
@RequiredArgsConstructor
public class RegistroClienteService {


    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final UsuarioRepository usuarioRepository;


    @Transactional
    public Cliente registrar(RegistroClienteDTO datos) {


        if (datos.getPassword() == null
                || !datos.getPassword().equals(
                datos.getConfirmarPassword())) {


            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden"
            );
        }


        String correo = datos.getCorreo() == null
                ? null
                : datos.getCorreo().trim().toLowerCase();


        if (correo != null
                && usuarioRepository.existsByEmail(correo)) {


            throw new CorreoEnUsoException(
                    "Ya existe una cuenta registrada "
                            + "con ese correo electrónico"
            );
        }


        // Valida nombre, formato de correo y política de contraseña,
        // cifra con BCrypt y asigna el rol CLIENTE.
        Usuario usuario = usuarioService.crearCuentaCliente(
                datos.getNombre(),
                correo,
                datos.getPassword()
        );


        return clienteService
                .buscarSinCuentaPorCorreo(correo)
                .map(cliente -> clienteService.vincularCuenta(
                        cliente,
                        usuario
                ))
                .orElseGet(() -> {


                    Cliente cliente = new Cliente();


                    cliente.setNombre(usuario.getNombre());
                    cliente.setCorreo(usuario.getEmail());
                    cliente.setUsuario(usuario);


                    return clienteService.registrarCliente(cliente);
                });
    }
}

