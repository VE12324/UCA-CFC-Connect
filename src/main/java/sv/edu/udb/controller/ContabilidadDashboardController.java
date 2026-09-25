package sv.edu.udb.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sv.edu.udb.service.PagoService;


@Controller
@RequiredArgsConstructor
public class ContabilidadDashboardController {


    private final PagoService pagoService;


    @GetMapping("/contabilidad/dashboard")
    public String mostrarDashboard(Model modelo) {
        modelo.addAttribute("totalRecaudado", pagoService.totalRecaudado());
        modelo.addAttribute("pagosPendientes", pagoService.contarPagosPendientes());
        modelo.addAttribute("pagosValidadosEsteMes", pagoService.contarPagadosEsteMes());
        return "contabilidad/dashboard";
    }
}
