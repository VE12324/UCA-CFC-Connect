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
import sv.edu.udb.model.Diplomado;
import sv.edu.udb.repository.CategoriaRepository;
import sv.edu.udb.repository.DocenteRepository;
import sv.edu.udb.repository.ModalidadRepository;
import sv.edu.udb.service.DiplomadoService;

@Controller
@RequiredArgsConstructor
public class DiplomadoWebController {

    private final DiplomadoService diplomadoService;
    private final CategoriaRepository categoriaRepository;
    private final ModalidadRepository modalidadRepository;
    private final DocenteRepository docenteRepository;

    private void cargarCatalogos(Model modelo) {
        modelo.addAttribute("categorias", categoriaRepository.findAll());
        modelo.addAttribute("modalidades", modalidadRepository.findAll());
        modelo.addAttribute("docentes", docenteRepository.findAll());
    }

    @GetMapping("/diplomados")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(pagina, 10, Sort.by("nombre").ascending());
        Page<Diplomado> diplomados = diplomadoService.listarDiplomados(buscar, paginacion);

        modelo.addAttribute("paginaDiplomados", diplomados);
        modelo.addAttribute("buscar", buscar);
        return "diplomados/inicio";
    }

    @GetMapping("/diplomados/crear")
    public String mostrarCrear(Model modelo) {
        modelo.addAttribute("diplomado", new Diplomado());
        cargarCatalogos(modelo);
        return "diplomados/crear";
    }

    @PostMapping("/diplomados/crear")
    public String crearDiplomado(
            @Valid @ModelAttribute("diplomado") Diplomado diplomado,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            cargarCatalogos(modelo);
            return "diplomados/crear";
        }

        try {
            diplomadoService.registrarDiplomado(diplomado);
            mensaje.addFlashAttribute("exito", "Diplomado registrado correctamente");
            return "redirect:/diplomados";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("fechaFin", "fechas.invalidas", excepcion.getMessage());
            cargarCatalogos(modelo);
            return "diplomados/crear";
        }
    }

    @GetMapping("/diplomados/actualizar/{id}")
    public String mostrarActualizar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("diplomado", diplomadoService.buscarPorId(id));
        cargarCatalogos(modelo);
        return "diplomados/actualizar";
    }

    @PostMapping("/diplomados/actualizar/{id}")
    public String actualizarDiplomado(
            @PathVariable Integer id,
            @Valid @ModelAttribute("diplomado") Diplomado diplomado,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            cargarCatalogos(modelo);
            return "diplomados/actualizar";
        }

        try {
            diplomadoService.actualizarDiplomado(id, diplomado);
            mensaje.addFlashAttribute("exito", "Diplomado actualizado correctamente");
            return "redirect:/diplomados";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("fechaFin", "fechas.invalidas", excepcion.getMessage());
            cargarCatalogos(modelo);
            return "diplomados/actualizar";
        }
    }

    @GetMapping("/diplomados/consultar/{id}")
    public String mostrarDiplomado(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("diplomado", diplomadoService.buscarPorId(id));
        return "diplomados/consultar";
    }

    @GetMapping("/diplomados/eliminar/{id}")
    public String mostrarEliminar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("diplomado", diplomadoService.buscarPorId(id));
        return "diplomados/eliminar";
    }

    @PostMapping("/diplomados/eliminar/{id}")
    public String eliminarDiplomado(@PathVariable Integer id, RedirectAttributes mensaje) {
        diplomadoService.eliminarDiplomado(id);
        mensaje.addFlashAttribute("exito", "Diplomado eliminado correctamente");
        return "redirect:/diplomados";
    }
}