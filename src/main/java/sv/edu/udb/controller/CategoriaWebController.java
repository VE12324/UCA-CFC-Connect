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
import sv.edu.udb.model.Categoria;
import sv.edu.udb.service.CategoriaService;

@Controller
@RequiredArgsConstructor
public class CategoriaWebController {

    private final CategoriaService categoriaService;

    @GetMapping("/categorias")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(pagina, 10, Sort.by("nombre").ascending());

        Page<Categoria> categorias = categoriaService.listarCategorias(buscar, paginacion);

        modelo.addAttribute("paginaCategorias", categorias);
        modelo.addAttribute("buscar", buscar);

        return "categorias/inicio";
    }

    @GetMapping("/categorias/crear")
    public String mostrarCrear(Model modelo) {
        modelo.addAttribute("categoria", new Categoria());
        return "categorias/crear";
    }

    @PostMapping("/categorias/crear")
    public String crearCategoria(
            @Valid @ModelAttribute("categoria") Categoria categoria,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "categorias/crear";
        }

        try {
            categoriaService.registrarCategoria(categoria);
            mensaje.addFlashAttribute("exito", "Categoría registrada correctamente");
            return "redirect:/categorias";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("nombre", "nombre.repetido", excepcion.getMessage());
            return "categorias/crear";
        }
    }

    @GetMapping("/categorias/actualizar/{id}")
    public String mostrarActualizar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("categoria", categoriaService.buscarPorId(id));
        return "categorias/actualizar";
    }

    @PostMapping("/categorias/actualizar/{id}")
    public String actualizarCategoria(
            @PathVariable Integer id,
            @Valid @ModelAttribute("categoria") Categoria categoria,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "categorias/actualizar";
        }

        try {
            categoriaService.actualizarCategoria(id, categoria);
            mensaje.addFlashAttribute("exito", "Categoría actualizada correctamente");
            return "redirect:/categorias";
        } catch (IllegalArgumentException excepcion) {
            resultado.rejectValue("nombre", "nombre.repetido", excepcion.getMessage());
            return "categorias/actualizar";
        }
    }

    @GetMapping("/categorias/consultar/{id}")
    public String mostrarCategoria(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("categoria", categoriaService.buscarPorId(id));
        return "categorias/consultar";
    }

    @GetMapping("/categorias/eliminar/{id}")
    public String mostrarEliminar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("categoria", categoriaService.buscarPorId(id));
        return "categorias/eliminar";
    }

    @PostMapping("/categorias/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Integer id, RedirectAttributes mensaje) {
        try {
            categoriaService.eliminarCategoria(id);
            mensaje.addFlashAttribute("exito", "Categoría eliminada correctamente");
        } catch (Exception excepcion) {
            mensaje.addFlashAttribute("error", "No se puede eliminar la categoría porque tiene cursos o diplomados asociados.");
        }
        return "redirect:/categorias";
    }
}
