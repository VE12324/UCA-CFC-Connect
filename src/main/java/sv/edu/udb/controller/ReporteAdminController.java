package sv.edu.udb.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sv.edu.udb.service.CotizacionService;
import sv.edu.udb.service.DashboardService;


@Controller
@RequiredArgsConstructor
public class ReporteAdminController {


    private final DashboardService dashboardService;
    private final CotizacionService cotizacionService;


    @GetMapping("/admin/reportes")
    public String mostrarReportes(Model modelo) {
        modelo.addAttribute("cursosActivos", dashboardService.countCursosActivos());
        modelo.addAttribute("diplomadosActivos", dashboardService.countDiplomadosActivos());
        modelo.addAttribute("espaciosDisponibles", dashboardService.countEspaciosDisponibles());
        modelo.addAttribute("cotizacionesPendientes", cotizacionService.contarPendientesDeAprobacion());
        return "admin/reportes";
    }
}


