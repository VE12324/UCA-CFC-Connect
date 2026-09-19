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

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("nombre").ascending()
        );

        Page<Espacio> espacios =
                espacioService.listarEspacios(
                        buscar,
                        paginacion
                );

        modelo.addAttribute(
                "paginaEspacios",
                espacios
        );

        modelo.addAttribute(
                "buscar",
                buscar
        );

        return "espacios/inicio";
    }

    @GetMapping("/espacios/crear")
    public String mostrarCrear(Model modelo) {

        Espacio espacio = new Espacio();
        espacio.setEstado("DISPONIBLE");

        modelo.addAttribute(
                "espacio",
                espacio
        );

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

        try {

            espacioService.registrarEspacio(espacio);

            mensaje.addFlashAttribute(
                    "exito",
                    "Espacio registrado correctamente"
            );

            return "redirect:/espacios";

        } catch (IllegalArgumentException excepcion) {

            resultado.rejectValue(
                    "nombre",
                    "nombre.repetido",
                    excepcion.getMessage()
            );

            return "espacios/crear";
        }
    }

    @GetMapping("/espacios/actualizar/{id}")
    public String mostrarActualizar(
            @PathVariable Integer id,
            Model modelo) {

        Espacio espacio =
                espacioService.buscarPorId(id);

        modelo.addAttribute(
                "espacio",
                espacio
        );

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

        try {

            espacioService.actualizarEspacio(
                    id,
                    espacio
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Espacio actualizado correctamente"
            );

            return "redirect:/espacios";

        } catch (IllegalArgumentException excepcion) {

            resultado.rejectValue(
                    "nombre",
                    "nombre.repetido",
                    excepcion.getMessage()
            );

            return "espacios/actualizar";
        }
    }

    @GetMapping("/espacios/consultar/{id}")
    public String mostrarEspacio(
            @PathVariable Integer id,
            Model modelo) {

        Espacio espacio =
                espacioService.buscarPorId(id);

        modelo.addAttribute(
                "espacio",
                espacio
        );

        return "espacios/consultar";
    }

    @GetMapping("/espacios/eliminar/{id}")
    public String mostrarEliminar(
            @PathVariable Integer id,
            Model modelo) {

        Espacio espacio =
                espacioService.buscarPorId(id);

        modelo.addAttribute(
                "espacio",
                espacio
        );

        return "espacios/eliminar";
    }

    @PostMapping("/espacios/eliminar/{id}")
    public String eliminarEspacio(
            @PathVariable Integer id,
            RedirectAttributes mensaje) {

        try {

            espacioService.eliminarEspacio(id);

            mensaje.addFlashAttribute(
                    "exito",
                    "Espacio eliminado correctamente"
            );

        } catch (Exception excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    "No se puede eliminar el espacio porque tiene alquileres u otros registros asociados."
            );
        }

        return "redirect:/espacios";
    }
}