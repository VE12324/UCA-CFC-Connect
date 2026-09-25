package sv.edu.udb.service;

import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.UsuarioDTO;
import sv.edu.udb.model.Rol;
import sv.edu.udb.model.Usuario;
import sv.edu.udb.repository.RolRepository;
import sv.edu.udb.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioService {

   private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PATRON_PASSWORD
            = Pattern.compile(
                    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)"
                    + "(?=.*[@$!%*?&])"
                    + "[A-Za-z\\d@$!%*?&]{8,}$"
            );

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Integer id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    "Usuario no encontrado con ID: " + id
                ));
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorCorreo(String correo) {

        return usuarioRepository
                .findByEmail(normalizarCorreo(correo))
                .orElseThrow(() -> new RuntimeException(
                    "No se encontró una cuenta "
                    + "con el correo indicado"
                ));
    }

    @Transactional
    public Usuario guardar(UsuarioDTO dto) {

        String correo = normalizarCorreo(
                dto.getCorreo()
        );

        validarCorreoDisponible(
                correo,
                dto.getId()
        );

        Rol rol = rolRepository
                .findById(dto.getIdRol())
                .orElseThrow(() -> new RuntimeException(
                    "Rol no encontrado"
                ));

        Usuario usuario;

        if (dto.getId() == null) {

            validarPassword(dto.getPassword());

            usuario = new Usuario();
            usuario.setEstado(true);

        } else {

            usuario = obtenerPorId(dto.getId());
        }

        usuario.setNombre(
                dto.getNombre().trim()
        );

        usuario.setEmail(correo);
        usuario.setRol(rol);

        if (dto.getPassword() != null
                && !dto.getPassword().isBlank()) {

            validarPassword(dto.getPassword());

            usuario.setPassword(
                    passwordEncoder.encode(
                            dto.getPassword()
                    )
            );
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario crearCuentaCliente(
            String nombre,
            String correo,
            String password) {

        validarNombre(nombre);

        String correoNormalizado
                = normalizarCorreo(correo);

        validarCorreo(correoNormalizado);

        validarCorreoDisponible(
                correoNormalizado,
                null
        );

        validarPassword(password);

        Rol rolCliente = rolRepository
                .findByNombre("CLIENTE")
                .orElseThrow(() -> new IllegalArgumentException(
                    "No se encontró el rol CLIENTE "
                    + "en la base de datos"
                ));

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre.trim());
        usuario.setEmail(correoNormalizado);

        usuario.setPassword(
                passwordEncoder.encode(password)
        );

        usuario.setRol(rolCliente);
        usuario.setEstado(true);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarCuentaCliente(
            Usuario usuario,
            String nombre,
            String correo,
            String nuevaPassword) {

        if (usuario == null
                || usuario.getId() == null) {

            throw new IllegalArgumentException(
                    "El cliente no tiene "
                    + "una cuenta asociada"
            );
        }

        validarNombre(nombre);

        String correoNormalizado
                = normalizarCorreo(correo);

        validarCorreo(correoNormalizado);

        validarCorreoDisponible(
                correoNormalizado,
                usuario.getId()
        );

        usuario.setNombre(nombre.trim());
        usuario.setEmail(correoNormalizado);

        if (nuevaPassword != null
                && !nuevaPassword.isBlank()) {

            validarPassword(nuevaPassword);

            usuario.setPassword(
                    passwordEncoder.encode(
                            nuevaPassword
                    )
            );
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Integer id) {

        if (!usuarioRepository.existsById(id)) {

            throw new RuntimeException(
                    "Usuario no encontrado con ID: " + id
            );
        }

        usuarioRepository.deleteById(id);
    }

    private void validarCorreoDisponible(
            String correo,
            Integer idUsuarioActual) {

        usuarioRepository.findByEmail(correo)
                .filter(usuario
                        -> idUsuarioActual == null
                        || !usuario.getId()
                                .equals(idUsuarioActual)
                )
                .ifPresent(usuario -> {
                    throw new IllegalArgumentException(
                            "El correo ya está registrado "
                            + "en otra cuenta"
                    );
                });
    }

    private void validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio "
                    + "para crear la cuenta"
            );
        }

        if (nombre.trim().length() > 80) {

            throw new IllegalArgumentException(
                    "El nombre de la cuenta no puede "
                    + "superar los 80 caracteres"
            );
        }
    }

    private void validarCorreo(String correo) {

        if (correo == null || correo.isBlank()) {

            throw new IllegalArgumentException(
                    "El correo es obligatorio "
                    + "para crear la cuenta"
            );
        }

        if (correo.length() > 100) {

            throw new IllegalArgumentException(
                    "El correo de la cuenta no puede "
                    + "superar los 100 caracteres"
            );
        }

        if (!correo.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )) {

            throw new IllegalArgumentException(
                    "El correo electrónico no tiene "
                    + "un formato válido"
            );
        }
    }

    private void validarPassword(String password) {

        if (password == null || password.isBlank()) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria"
            );
        }

        if (!PATRON_PASSWORD
                .matcher(password)
                .matches()) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener mínimo "
                    + "8 caracteres, una mayúscula, "
                    + "una minúscula, un número y "
                    + "un carácter especial (@$!%*?&)"
            );
        }
    }

    private String normalizarCorreo(String correo) {

        if (correo == null) {
            return null;
        }

        return correo.trim().toLowerCase();
    }
}
