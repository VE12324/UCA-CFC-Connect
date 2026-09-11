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
import sv.edu.udb.model.Espacio;
import sv.edu.udb.service.EspacioService;

@Controller
@RequiredArgsConstructor
public class EspacioWebController {

    private final EspacioService espacioService;

    @GetMapping("/espacios")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(pagina, 10, Sort.by("nombre").ascending());
        Page<Espacio> espacios = espacioService.listarEspacios(buscar, paginacion);

        modelo.addAttribute("paginaEspacios", espacios);
        modelo.addAttribute("buscar", buscar);
        return "espacios/inicio";
    }

    @GetMapping("/espacios/crear")
    public String mostrarCrear(Model modelo) {
        modelo.addAttribute("espacio", new Espacio());
        return "espacios/crear";
    }

    @PostMapping("/espacios/crear")
    public String crearEspacio(
            @Valid @ModelAttribute("espacio") Espacio espacio,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "espacios/crear";
        }

        espacioService.registrarEspacio(espacio);
        mensaje.addFlashAttribute("exito", "Espacio registrado correctamente");
        return "redirect:/espacios";
    }

    @GetMapping("/espacios/actualizar/{id}")
    public String mostrarActualizar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("espacio", espacioService.buscarPorId(id));
        return "espacios/actualizar";
    }

    @PostMapping("/espacios/actualizar/{id}")
    public String actualizarEspacio(
            @PathVariable Integer id,
            @Valid @ModelAttribute("espacio") Espacio espacio,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "espacios/actualizar";
        }

        espacioService.actualizarEspacio(id, espacio);
        mensaje.addFlashAttribute("exito", "Espacio actualizado correctamente");
        return "redirect:/espacios";
    }

    @GetMapping("/espacios/consultar/{id}")
    public String mostrarEspacio(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("espacio", espacioService.buscarPorId(id));
        return "espacios/consultar";
    }

    @GetMapping("/espacios/eliminar/{id}")
    public String mostrarEliminar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("espacio", espacioService.buscarPorId(id));
        return "espacios/eliminar";
    }

    @PostMapping("/espacios/eliminar/{id}")
    public String eliminarEspacio(@PathVariable Integer id, RedirectAttributes mensaje) {
        espacioService.eliminarEspacio(id);
        mensaje.addFlashAttribute("exito", "Espacio eliminado correctamente");
        return "redirect:/espacios";
    }
}
