package sv.edu.udb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sv.edu.udb.dto.CotizacionRespuestaDTO;
import sv.edu.udb.service.CotizacionService;


@Controller
@RequiredArgsConstructor
public class CotizacionAdminWebController {


    private final CotizacionService cotizacionService;


    @GetMapping("/admin/cotizaciones")
    public String mostrarInicio(
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {


        Pageable paginacion = PageRequest.of(pagina, 10);


        Page<CotizacionRespuestaDTO> cotizaciones = cotizacionService.listarPendientesOEnProceso(paginacion);


        modelo.addAttribute("paginaCotizaciones", cotizaciones);


        return "cotizaciones/inicio";
    }


    @PostMapping("/admin/cotizaciones/{id}/aprobar")
    public String aprobar(@PathVariable Integer id, RedirectAttributes mensaje) {
        try {
            cotizacionService.aprobarCotizacion(id);
            mensaje.addFlashAttribute("exito", "Cotización aprobada correctamente");
        } catch (IllegalArgumentException excepcion) {
            mensaje.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/admin/cotizaciones";
    }


    @PostMapping("/admin/cotizaciones/{id}/rechazar")
    public String rechazar(@PathVariable Integer id, RedirectAttributes mensaje) {
        try {
            cotizacionService.rechazarCotizacion(id);
            mensaje.addFlashAttribute("exito", "Cotización rechazada correctamente");
        } catch (IllegalArgumentException excepcion) {
            mensaje.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/admin/cotizaciones";
    }
}

