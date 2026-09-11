package sv.edu.udb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.service.CateringService;

@Controller
@RequiredArgsConstructor
public class CateringWebController {

    private final CateringService cateringService;

    @GetMapping("/catering")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(pagina, 10, Sort.by("nombre").ascending());
        Page<ServicioCatering> servicios = cateringService.listarServicios(buscar, paginacion);

        modelo.addAttribute("paginaCatering", servicios);
        modelo.addAttribute("buscar", buscar);
        return "catering/inicio";
    }

    @GetMapping("/catering/crear")
    public String mostrarCrear(Model modelo) {
        modelo.addAttribute("servicio", new ServicioCatering());
        return "catering/crear";
    }

    @PostMapping("/catering/crear")
    public String crearServicio(
            @Valid @ModelAttribute("servicio") ServicioCatering servicio,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "catering/crear";
        }

        try {
            cateringService.registrarServicio(servicio);
            mensaje.addFlashAttribute("exito", "Servicio de catering registrado correctamente");
            return "redirect:/catering";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("capacidadMaxima", "capacidad.invalida", excepcion.getMessage());
            return "catering/crear";
        }
    }

    @GetMapping("/catering/actualizar/{id}")
    public String mostrarActualizar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("servicio", cateringService.buscarPorId(id));
        return "catering/actualizar";
    }

    @PostMapping("/catering/actualizar/{id}")
    public String actualizarServicio(
            @PathVariable Integer id,
            @Valid @ModelAttribute("servicio") ServicioCatering servicio,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "catering/actualizar";
        }

        try {
            cateringService.actualizarServicio(id, servicio);
            mensaje.addFlashAttribute("exito", "Servicio de catering actualizado correctamente");
            return "redirect:/catering";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("capacidadMaxima", "capacidad.invalida", excepcion.getMessage());
            return "catering/actualizar";
        }
    }

    @GetMapping("/catering/consultar/{id}")
    public String mostrarServicio(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("servicio", cateringService.buscarPorId(id));
        return "catering/consultar";
    }

    @GetMapping("/catering/eliminar/{id}")
    public String mostrarEliminar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("servicio", cateringService.buscarPorId(id));
        return "catering/eliminar";
    }

    @PostMapping("/catering/eliminar/{id}")
    public String eliminarServicio(@PathVariable Integer id, RedirectAttributes mensaje) {
        cateringService.eliminarServicio(id);
        mensaje.addFlashAttribute("exito", "Servicio de catering eliminado correctamente");
        return "redirect:/catering";
    }
}