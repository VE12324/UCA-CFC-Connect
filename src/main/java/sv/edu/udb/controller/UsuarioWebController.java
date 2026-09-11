package sv.edu.udb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sv.edu.udb.dto.UsuarioDTO;
import sv.edu.udb.service.UsuarioService;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioWebController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/crear")
    public String mostrarCrear(Model model) {
        model.addAttribute("usuario", new UsuarioDTO());
        model.addAttribute("roles", usuarioService.listarRoles());
        return "usuarios/crear";
    }

    @PostMapping("/crear")
    public String crearUsuario(
            @Valid @ModelAttribute("usuario") UsuarioDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes mensaje) {

        if (result.hasErrors()) {
            model.addAttribute("roles", usuarioService.listarRoles());
            return "usuarios/crear";
        }

        try {
            usuarioService.guardar(dto);
            mensaje.addFlashAttribute("exito", "Usuario creado correctamente");
            return "redirect:/usuarios";
        } catch (RuntimeException e) {
            result.rejectValue("correo", "correo.duplicado", e.getMessage());
            model.addAttribute("roles", usuarioService.listarRoles());
            return "usuarios/crear";
        }
    }

    @GetMapping("/consultar/{id}")
    public String mostrarUsuario(@PathVariable Integer id, Model model) {
        model.addAttribute("usuario", usuarioService.obtenerPorId(id));
        return "usuarios/consultar";
    }

    @GetMapping("/actualizar/{id}")
    public String mostrarActualizar(@PathVariable Integer id, Model model) {
        var usuario = usuarioService.obtenerPorId(id);
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setCorreo(usuario.getEmail());
        dto.setIdRol(usuario.getRol().getIdRol());

        model.addAttribute("usuario", dto);
        model.addAttribute("roles", usuarioService.listarRoles());
        return "usuarios/actualizar";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarUsuario(
            @PathVariable Integer id,
            @ModelAttribute("usuario") UsuarioDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes mensaje) {

        dto.setId(id);

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            result.rejectValue("nombre", "nombre.requerido", "El nombre es obligatorio");
        }
        if (dto.getCorreo() == null || dto.getCorreo().isBlank()) {
            result.rejectValue("correo", "correo.requerido", "El correo es obligatorio");
        }
        if (dto.getIdRol() == null) {
            result.rejectValue("idRol", "rol.requerido", "Debe seleccionar un rol");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", usuarioService.listarRoles());
            return "usuarios/actualizar";
        }

        try {
            usuarioService.guardar(dto);
            mensaje.addFlashAttribute("exito", "Usuario actualizado correctamente");
            return "redirect:/usuarios";
        } catch (RuntimeException e) {
            result.rejectValue("correo", "correo.duplicado", e.getMessage());
            model.addAttribute("roles", usuarioService.listarRoles());
            return "usuarios/actualizar";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String mostrarEliminar(@PathVariable Integer id, Model model) {
        model.addAttribute("usuario", usuarioService.obtenerPorId(id));
        return "usuarios/eliminar";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes mensaje) {
        usuarioService.eliminar(id);
        mensaje.addFlashAttribute("exito", "Usuario eliminado correctamente");
        return "redirect:/usuarios";
    }
}
