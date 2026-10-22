package sv.edu.udb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sv.edu.udb.dto.RegistroClienteDTO;
import sv.edu.udb.exception.CorreoEnUsoException;
import sv.edu.udb.service.RegistroClienteService;

@Controller
@RequiredArgsConstructor
public class RegistroWebController {

    private final RegistroClienteService registroClienteService;

    @GetMapping("/registro")
    public String mostrarRegistro(
            Authentication authentication,
            Model modelo) {

        if (tieneSesion(authentication)) {
            return "redirect:" + destinoPorRol(authentication);
        }

        modelo.addAttribute("registro", new RegistroClienteDTO());

        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @Valid @ModelAttribute("registro") RegistroClienteDTO registro,
            BindingResult resultado,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (tieneSesion(authentication)) {
            return "redirect:" + destinoPorRol(authentication);
        }

        if (registro.getConfirmarPassword() != null
                && !registro.getConfirmarPassword().isBlank()
                && !registro.getConfirmarPassword()
                .equals(registro.getPassword())) {

            resultado.rejectValue(
                    "confirmarPassword",
                    "coincidencia",
                    "Las contraseñas no coinciden"
            );
        }

        if (resultado.hasErrors()) {
            return "auth/registro";
        }

        try {
            registroClienteService.registrar(registro);

        } catch (CorreoEnUsoException excepcion) {

            resultado.rejectValue(
                    "correo",
                    "duplicado",
                    excepcion.getMessage()
            );

            return "auth/registro";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "registro",
                    excepcion.getMessage()
            );

            return "auth/registro";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Cuenta creada, ya puedes iniciar sesión"
        );

        redirectAttributes.addFlashAttribute(
                "correoRegistrado",
                registro.getCorreo().trim().toLowerCase()
        );

        return "redirect:/login";
    }

    private boolean tieneSesion(Authentication authentication) {

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String destinoPorRol(Authentication authentication) {

        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CLIENTE");

        return switch (rol) {
            case "ROLE_ADMIN" -> "/admin/dashboard";
            case "ROLE_RECEPCIONISTA" -> "/recepcionista/dashboard";
            case "ROLE_CONTABILIDAD" -> "/contabilidad/dashboard";
            case "ROLE_CLIENTE" -> "/";
            default -> "/login?sin-acceso";
        };
    }
}
