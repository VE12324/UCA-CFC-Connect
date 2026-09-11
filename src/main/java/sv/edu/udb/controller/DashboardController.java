package sv.edu.udb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sv.edu.udb.service.DashboardService;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/")
    public String raiz(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CLIENTE");
        return switch (rol) {
            case "ROLE_ADMIN", "ROLE_CONTABILIDAD" -> "redirect:/admin/dashboard";
            case "ROLE_RECEPCIONISTA" -> "redirect:/clientes";
            default -> "redirect:/login?sin-acceso";
        };
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("cursosActivos", dashboardService.countCursosActivos());
        model.addAttribute("diplomadosActivos", dashboardService.countDiplomadosActivos());
        model.addAttribute("espaciosDisponibles", dashboardService.countEspaciosDisponibles());
        model.addAttribute("totalUsuarios", dashboardService.countUsuarios());
        model.addAttribute("ultimosCursos", dashboardService.listarUltimosCursos());
        return "admin/dashboard";
    }
}
