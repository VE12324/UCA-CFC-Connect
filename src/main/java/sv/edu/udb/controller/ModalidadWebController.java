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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sv.edu.udb.model.Modalidad;
import sv.edu.udb.service.ModalidadService;


@Controller
@RequiredArgsConstructor
public class ModalidadWebController {


    private final ModalidadService modalidadService;


    @GetMapping("/modalidades")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {


        Pageable paginacion = PageRequest.of(pagina, 10, Sort.by("nombre").ascending());


        Page<Modalidad> modalidades = modalidadService.listarModalidades(buscar, paginacion);


        modelo.addAttribute("paginaModalidades", modalidades);
        modelo.addAttribute("buscar", buscar);


        return "modalidades/inicio";
    }


    @GetMapping("/modalidades/crear")
    public String mostrarCrear(Model modelo) {
        modelo.addAttribute("modalidad", new Modalidad());
        return "modalidades/crear";
    }


    @PostMapping("/modalidades/crear")
    public String crearModalidad(
            @Valid @ModelAttribute("modalidad") Modalidad modalidad,
            BindingResult resultado,
            RedirectAttributes mensaje) {


        if (resultado.hasErrors()) {
            return "modalidades/crear";
        }


        try {
            modalidadService.registrarModalidad(modalidad);
            mensaje.addFlashAttribute("exito", "Modalidad registrada correctamente");
            return "redirect:/modalidades";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("nombre", "nombre.repetido", excepcion.getMessage());
            return "modalidades/crear";
        }
    }


    @GetMapping("/modalidades/actualizar/{id}")
    public String mostrarActualizar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("modalidad", modalidadService.buscarPorId(id));
        return "modalidades/actualizar";
    }


    @PostMapping("/modalidades/actualizar/{id}")
    public String actualizarModalidad(
            @PathVariable Integer id,
            @Valid @ModelAttribute("modalidad") Modalidad modalidad,
            BindingResult resultado,
            RedirectAttributes mensaje) {


        if (resultado.hasErrors()) {
            return "modalidades/actualizar";
        }


        try {
            modalidadService.actualizarModalidad(id, modalidad);
            mensaje.addFlashAttribute("exito", "Modalidad actualizada correctamente");
            return "redirect:/modalidades";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("nombre", "nombre.repetido", excepcion.getMessage());
            return "modalidades/actualizar";
        }
    }


    @GetMapping("/modalidades/consultar/{id}")
    public String mostrarModalidad(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("modalidad", modalidadService.buscarPorId(id));
        return "modalidades/consultar";
    }


    @GetMapping("/modalidades/eliminar/{id}")
    public String mostrarEliminar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("modalidad", modalidadService.buscarPorId(id));
        return "modalidades/eliminar";
    }


    @PostMapping("/modalidades/eliminar/{id}")
    public String eliminarModalidad(@PathVariable Integer id, RedirectAttributes mensaje) {
        try {
            modalidadService.eliminarModalidad(id);
            mensaje.addFlashAttribute("exito", "Modalidad eliminada correctamente");
        } catch (Exception excepcion) {
            mensaje.addFlashAttribute("error", "No se puede eliminar la modalidad porque tiene cursos o diplomados asociados.");
        }
        return "redirect:/modalidades";
    }
}


