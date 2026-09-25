/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.service.AlquilerService;
/**
 *
 * @author crist
 */
@Controller
@RequiredArgsConstructor
public class AlquilerWebController {
    
    private final AlquilerService alquilerService;

    @GetMapping("/alquileres")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("horaInicio").descending()
        );

        Page<Alquiler> alquileres
                = alquilerService.listarAlquileres(
                        buscar,
                        paginacion
                );

        modelo.addAttribute(
                "paginaAlquileres",
                alquileres
        );

        modelo.addAttribute(
                "buscar",
                buscar
        );

        return "alquileres/inicio";
    }

    @GetMapping("/alquileres/crear")
    public String mostrarCrear(Model modelo) {

        LocalDateTime horaInicio
                = LocalDateTime.now()
                        .plusHours(1)
                        .truncatedTo(ChronoUnit.HOURS);

        Alquiler alquiler = new Alquiler();

        alquiler.setFecha(
                horaInicio.toLocalDate()
        );

        alquiler.setHoraInicio(
                horaInicio
        );

        alquiler.setHoraFin(
                horaInicio.plusHours(1)
        );

        alquiler.setCantidadPersonas(1);
        alquiler.setEstado("PENDIENTE");
        alquiler.setCliente(new Cliente());
        alquiler.setEspacio(new Espacio());

        modelo.addAttribute(
                "alquiler",
                alquiler
        );

        cargarCatalogos(modelo);

        return "alquileres/crear";
    }

    @PostMapping("/alquileres/crear")
    public String crearAlquiler(
            @Valid @ModelAttribute("alquiler")
            Alquiler alquiler,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {

            cargarCatalogos(modelo);

            return "alquileres/crear";
        }

        try {
            alquilerService.registrarAlquiler(
                    alquiler
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Alquiler registrado correctamente"
            );

            return "redirect:/alquileres";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "alquiler.invalido",
                    excepcion.getMessage()
            );

            cargarCatalogos(modelo);

            return "alquileres/crear";
        }
    }

    @GetMapping("/alquileres/actualizar/{id}")
    public String mostrarActualizar(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "alquiler",
                alquilerService.buscarPorId(id)
        );

        cargarCatalogos(modelo);

        return "alquileres/actualizar";
    }

    @PostMapping("/alquileres/actualizar/{id}")
    public String actualizarAlquiler(
            @PathVariable Integer id,
            @Valid @ModelAttribute("alquiler")
            Alquiler alquiler,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        alquiler.setIdAlquiler(id);

        if (resultado.hasErrors()) {

            cargarCatalogos(modelo);

            return "alquileres/actualizar";
        }

        try {
            alquilerService.actualizarAlquiler(
                    id,
                    alquiler
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Alquiler actualizado correctamente"
            );

            return "redirect:/alquileres";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "alquiler.invalido",
                    excepcion.getMessage()
            );

            cargarCatalogos(modelo);

            return "alquileres/actualizar";
        }
    }

    @GetMapping("/alquileres/consultar/{id}")
    public String mostrarAlquiler(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "alquiler",
                alquilerService.buscarPorId(id)
        );

        return "alquileres/consultar";
    }

    @GetMapping("/alquileres/eliminar/{id}")
    public String mostrarEliminar(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "alquiler",
                alquilerService.buscarPorId(id)
        );

        return "alquileres/eliminar";
    }

    @PostMapping("/alquileres/eliminar/{id}")
    public String eliminarAlquiler(
            @PathVariable Integer id,
            RedirectAttributes mensaje) {

        try {
            alquilerService.eliminarAlquiler(id);

            mensaje.addFlashAttribute(
                    "exito",
                    "Alquiler eliminado correctamente"
            );

        } catch (IllegalArgumentException excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    excepcion.getMessage()
            );

        } catch (Exception excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    "No se pudo eliminar el alquiler"
            );
        }

        return "redirect:/alquileres";
    }

    private void cargarCatalogos(Model modelo) {

        modelo.addAttribute(
                "clientes",
                alquilerService.listarClientes()
        );

        modelo.addAttribute(
                "espacios",
                alquilerService.listarEspacios()
        );
    }
}
