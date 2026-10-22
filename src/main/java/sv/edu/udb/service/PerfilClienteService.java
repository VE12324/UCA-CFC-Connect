package sv.edu.udb.service;


import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.FormatoContacto;
import sv.edu.udb.dto.PerfilClienteDTO;
import sv.edu.udb.exception.CampoInvalidoException;
import sv.edu.udb.exception.ClienteNoVinculadoException;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Usuario;
import sv.edu.udb.repository.ClienteRepository;


/**
 * Datos del cliente con sesión iniciada: búsqueda por la cuenta, detección
 * de registro incompleto y edición de "Mis datos".
 */
@Service
@RequiredArgsConstructor
public class PerfilClienteService {


    private final ClienteRepository clienteRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    public Optional<Cliente> buscarCliente(String correoSesion) {


        if (correoSesion == null || correoSesion.isBlank()) {
            return Optional.empty();
        }


        return clienteRepository.findByUsuario_EmailIgnoreCase(
                correoSesion.trim()
        );
    }


    @Transactional(readOnly = true)
    public Cliente obtenerCliente(String correoSesion) {


        return buscarCliente(correoSesion)
                .orElseThrow(ClienteNoVinculadoException::new);
    }


    /** Registro completo = DUI/NIT, teléfono y dirección. */
    public boolean estaIncompleto(Cliente cliente) {


        return cliente == null
                || vacio(cliente.getDui())
                || vacio(cliente.getTelefono())
                || vacio(cliente.getDireccion());
    }


    public PerfilClienteDTO cargarFormulario(Cliente cliente) {


        Usuario usuario = cliente.getUsuario();


        return new PerfilClienteDTO(
                usuario != null ? usuario.getNombre() : cliente.getNombre(),
                cliente.getDui(),
                cliente.getEmpresa(),
                usuario != null ? usuario.getEmail() : cliente.getCorreo(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                null
        );
    }


    /**
     * Guarda "Mis datos". Devuelve true si cambió el correo (el controlador
     * cierra la sesión, porque el correo es el usuario de inicio de sesión).
     */
    @Transactional
    public boolean actualizar(
            String correoSesion,
            PerfilClienteDTO datos) {


        Cliente cliente = obtenerCliente(correoSesion);
        Usuario usuario = cliente.getUsuario();


        String dui = limpiar(datos.getDui());
        String telefono = limpiar(datos.getTelefono());


        // Mismas reglas que el DTO, por si el servicio se llama sin pasar
        // por la validación del formulario.
        if (dui == null || !FormatoContacto.duiNitValido(dui)) {
            throw new CampoInvalidoException(
                    "dui",
                    FormatoContacto.MENSAJE_DUI_NIT
            );
        }


        if (telefono == null || !FormatoContacto.telefonoValido(telefono)) {
            throw new CampoInvalidoException(
                    "telefono",
                    FormatoContacto.MENSAJE_TELEFONO
            );
        }


        telefono = FormatoContacto.normalizarTelefono(telefono);


        clienteRepository.findByDui(dui)
                .filter(otro -> !otro.getIdCliente()
                        .equals(cliente.getIdCliente()))
                .ifPresent(otro -> {
                    throw new CampoInvalidoException(
                            "dui",
                            "Ya existe un cliente registrado "
                                    + "con el DUI/NIT: " + dui
                    );
                });


        String correoNuevo = datos.getCorreo() == null
                ? null
                : datos.getCorreo().trim().toLowerCase();


        boolean cambioCorreo = correoNuevo != null
                && !correoNuevo.equalsIgnoreCase(usuario.getEmail());


        if (cambioCorreo) {


            if (vacio(datos.getPasswordActual())
                    || !passwordEncoder.matches(
                    datos.getPasswordActual(),
                    usuario.getPassword())) {


                throw new CampoInvalidoException(
                        "passwordActual",
                        "La contraseña actual no es correcta. "
                                + "Es necesaria para cambiar el correo."
                );
            }
        }


        try {
            usuarioService.actualizarCuentaCliente(
                    usuario,
                    datos.getNombre(),
                    correoNuevo,
                    null
            );


        } catch (IllegalArgumentException excepcion) {


            String mensaje = excepcion.getMessage();


            throw new CampoInvalidoException(
                    mensaje != null && mensaje.contains("correo")
                            ? "correo"
                            : "nombre",
                    mensaje
            );
        }


        cliente.setNombre(usuario.getNombre());
        cliente.setCorreo(usuario.getEmail());
        cliente.setDui(dui);
        cliente.setEmpresa(limpiar(datos.getEmpresa()));
        cliente.setTelefono(telefono);
        cliente.setDireccion(limpiar(datos.getDireccion()));


        clienteRepository.save(cliente);


        return cambioCorreo;
    }


    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }


    private static String limpiar(String texto) {
        return vacio(texto) ? null : texto.trim();
    }
}

