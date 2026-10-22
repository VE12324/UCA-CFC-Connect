package sv.edu.udb.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sv.edu.udb.dto.FormatoContacto;
import sv.edu.udb.dto.PerfilClienteDTO;
import sv.edu.udb.exception.CampoInvalidoException;
import sv.edu.udb.service.PerfilClienteService;

@Controller
@RequestMapping("/mi-cuenta/datos")
@RequiredArgsConstructor
public class MisDatosController implements PaginaPortalCliente {

    private final PerfilClienteService perfilClienteService;

    // Patrones y mensajes del formulario (Thymeleaf no permite T(...) en las
    // plantillas, así que se pasan desde aquí).
    @ModelAttribute("formatoContacto")
    public Map<String, String> formatoContacto() {
        return Map.of(
                "duiNit", FormatoContacto.DUI_NIT,
                "mensajeDuiNit", FormatoContacto.MENSAJE_DUI_NIT,
                "telefono", FormatoContacto.TELEFONO,
                "mensajeTelefono", FormatoContacto.MENSAJE_TELEFONO
        );
    }

    @GetMapping
    public String mostrarFormulario(
            Authentication authentication,
            Model modelo) {

        modelo.addAttribute(
                "perfil",
                perfilClienteService.cargarFormulario(
                        perfilClienteService.obtenerCliente(
                                authentication.getName()
                        )
                )
        );

        return "mi-cuenta/datos";
    }

    @PostMapping
    public String guardar(
            @Valid @ModelAttribute("perfil") PerfilClienteDTO perfil,
            BindingResult resultado,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return "mi-cuenta/datos";
        }

        boolean cambioCorreo;

        try {
            cambioCorreo = perfilClienteService.actualizar(
                    authentication.getName(),
                    perfil
            );

        } catch (CampoInvalidoException excepcion) {

            resultado.rejectValue(
                    excepcion.getCampo(),
                    "invalido",
                    excepcion.getMessage()
            );

            return "mi-cuenta/datos";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject("perfil", excepcion.getMessage());

            return "mi-cuenta/datos";
        }

        if (cambioCorreo) {

            // El correo es el usuario de inicio de sesión: se cierra la
            // sesión para que vuelva a entrar con el correo nuevo.
            new SecurityContextLogoutHandler()
                    .logout(request, response, authentication);

            return "redirect:/login?correoActualizado";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Tus datos se guardaron correctamente."
        );

        return "redirect:/mi-cuenta/datos";
    }
}
