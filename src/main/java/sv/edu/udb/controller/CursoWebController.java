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
import sv.edu.udb.model.Curso;
import sv.edu.udb.repository.CategoriaRepository;
import sv.edu.udb.repository.DocenteRepository;
import sv.edu.udb.repository.ModalidadRepository;
import sv.edu.udb.service.CursoService;

@Controller
@RequiredArgsConstructor
public class CursoWebController {
    private final CursoService cursoService;
    private final CategoriaRepository categoriaRepository;
    private final ModalidadRepository modalidadRepository;
    private final DocenteRepository docenteRepository;

    private void cargarCatalogos(Model modelo) {
        modelo.addAttribute("categorias", categoriaRepository.findAll());
        modelo.addAttribute("modalidades", modalidadRepository.findAll());
        modelo.addAttribute("docentes", docenteRepository.findAll());
    }

    @GetMapping("/cursos")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(pagina, 10, Sort.by("nombre").ascending());
        Page<Curso> cursos = cursoService.listarCursos(buscar, paginacion);

        modelo.addAttribute("paginaCursos", cursos);
        modelo.addAttribute("buscar", buscar);
        return "cursos/inicio";
    }

    @GetMapping("/cursos/crear")
    public String mostrarCrear(Model modelo) {
        modelo.addAttribute("curso", new Curso());
        cargarCatalogos(modelo);
        return "cursos/crear";
    }


    @PostMapping("/cursos/crear")
    public String crearCurso(
            @Valid @ModelAttribute("curso") Curso curso,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            cargarCatalogos(modelo);
            return "cursos/crear";
        }

        try {
            cursoService.registrarCurso(curso);
            mensaje.addFlashAttribute("exito", "Curso registrado correctamente");
            return "redirect:/cursos";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("fechaFin", "fechas.invalidas", excepcion.getMessage());
            cargarCatalogos(modelo);
            return "cursos/crear";
        }
    }

    @GetMapping("/cursos/actualizar/{id}")
    public String mostrarActualizar(@PathVariable Integer id, Model modelo) {
        Curso curso = cursoService.buscarPorId(id);
        modelo.addAttribute("curso", curso);
        cargarCatalogos(modelo);
        return "cursos/actualizar";
    }

    @PostMapping("/cursos/actualizar/{id}")
    public String actualizarCurso(
            @PathVariable Integer id,
            @Valid @ModelAttribute("curso") Curso curso,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            cargarCatalogos(modelo);
            return "cursos/actualizar";
        }

        try {
            cursoService.actualizarCurso(id, curso);
            mensaje.addFlashAttribute("exito", "Curso actualizado correctamente");
            return "redirect:/cursos";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("fechaFin", "fechas.invalidas", excepcion.getMessage());
            cargarCatalogos(modelo);
            return "cursos/actualizar";
        }
    }

    @GetMapping("/cursos/consultar/{id}")
    public String mostrarCurso(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("curso", cursoService.buscarPorId(id));
        return "cursos/consultar";
    }

    @GetMapping("/cursos/eliminar/{id}")
    public String mostrarEliminar(@PathVariable Integer id, Model modelo) {
        Curso curso = cursoService.buscarPorId(id);
        modelo.addAttribute("curso", curso);
        return "cursos/eliminar";
    }

    @PostMapping("/cursos/eliminar/{id}")
    public String eliminarCurso(@PathVariable Integer id, RedirectAttributes mensaje) {
        cursoService.eliminarCurso(id);
        mensaje.addFlashAttribute("exito", "Curso eliminado correctamente");
        return "redirect:/cursos";
    }
}
