package sv.edu.udb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sv.edu.udb.service.RecepcionistaDashboardService;

@Controller
@RequiredArgsConstructor
public class RecepcionistaDashboardController {

    private final RecepcionistaDashboardService recepcionistaDashboardService;

    @GetMapping("/recepcionista/dashboard")
    public String mostrarDashboard(Model modelo) {
        modelo.addAttribute("totalClientes", recepcionistaDashboardService.contarClientes());
        modelo.addAttribute("totalParticipantes", recepcionistaDashboardService.contarParticipantes());
        modelo.addAttribute("cotizacionesPendientes", recepcionistaDashboardService.contarCotizacionesPendientes());
        modelo.addAttribute("totalInscripciones", recepcionistaDashboardService.contarInscripciones());
        modelo.addAttribute("totalAlquileres", recepcionistaDashboardService.contarAlquileres());
        modelo.addAttribute("totalSolicitudesCatering", recepcionistaDashboardService.contarSolicitudesCatering());
        return "recepcionista/dashboard";
    }
}
