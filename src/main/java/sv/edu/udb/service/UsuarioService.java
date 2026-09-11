package sv.edu.udb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.UsuarioDTO;
import sv.edu.udb.model.Rol;
import sv.edu.udb.model.Usuario;
import sv.edu.udb.repository.RolRepository;
import sv.edu.udb.repository.UsuarioRepository;

import java.util.List;

@Service

public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Transactional
    public Usuario guardar(UsuarioDTO dto) {
        if (dto.getId() == null && usuarioRepository.existsByEmail(dto.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado en el sistema");
        }

        // CAMBIO AQUÍ: Convertimos dto.getIdRol() a Integer si dto.getIdRol() sigue siendo Integer
        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario();
        if (dto.getId() != null) {
            usuario = obtenerPorId(dto.getId());
        }

        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getCorreo());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(dto.getPassword());
        }
        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
