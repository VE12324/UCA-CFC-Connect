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
import sv.edu.udb.model.Docente;
import sv.edu.udb.service.DocenteService;


@Controller
@RequiredArgsConstructor
public class DocenteWebController {


    private final DocenteService docenteService;


    @GetMapping("/docentes")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {


        Pageable paginacion = PageRequest.of(pagina, 10, Sort.by("nombre").ascending());


        Page<Docente> docentes = docenteService.listarDocentes(buscar, paginacion);


        modelo.addAttribute("paginaDocentes", docentes);
        modelo.addAttribute("buscar", buscar);


        return "docentes/inicio";
    }


    @GetMapping("/docentes/crear")
    public String mostrarCrear(Model modelo) {
        modelo.addAttribute("docente", new Docente());
        return "docentes/crear";
    }


    @PostMapping("/docentes/crear")
    public String crearDocente(
            @Valid @ModelAttribute("docente") Docente docente,
            BindingResult resultado,
            RedirectAttributes mensaje) {


        if (resultado.hasErrors()) {
            return "docentes/crear";
        }


        try {
            docenteService.registrarDocente(docente);
            mensaje.addFlashAttribute("exito", "Docente registrado correctamente");
            return "redirect:/docentes";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("nombre", "nombre.repetido", excepcion.getMessage());
            return "docentes/crear";
        }
    }


    @GetMapping("/docentes/actualizar/{id}")
    public String mostrarActualizar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("docente", docenteService.buscarPorId(id));
        return "docentes/actualizar";
    }


    @PostMapping("/docentes/actualizar/{id}")
    public String actualizarDocente(
            @PathVariable Integer id,
            @Valid @ModelAttribute("docente") Docente docente,
            BindingResult resultado,
            RedirectAttributes mensaje) {


        if (resultado.hasErrors()) {
            return "docentes/actualizar";
        }


        try {
            docenteService.actualizarDocente(id, docente);
            mensaje.addFlashAttribute("exito", "Docente actualizado correctamente");
            return "redirect:/docentes";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("nombre", "nombre.repetido", excepcion.getMessage());
            return "docentes/actualizar";
        }
    }


    @GetMapping("/docentes/consultar/{id}")
    public String mostrarDocente(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("docente", docenteService.buscarPorId(id));
        return "docentes/consultar";
    }


    @GetMapping("/docentes/eliminar/{id}")
    public String mostrarEliminar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("docente", docenteService.buscarPorId(id));
        return "docentes/eliminar";
    }


    @PostMapping("/docentes/eliminar/{id}")
    public String eliminarDocente(@PathVariable Integer id, RedirectAttributes mensaje) {
        try {
            docenteService.eliminarDocente(id);
            mensaje.addFlashAttribute("exito", "Docente eliminado correctamente");
        } catch (Exception excepcion) {
            mensaje.addFlashAttribute("error", "No se puede eliminar el docente porque tiene cursos o diplomados asignados.");
        }
        return "redirect:/docentes";
    }
}
