/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import sv.edu.udb.model.Actividad;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Curso;
import sv.edu.udb.model.Diplomado;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.service.ActividadService;
/**
 *
 * @author crist
 */
@Controller
@RequiredArgsConstructor
public class ActividadWebController {
    
    private final ActividadService actividadService;

    @GetMapping("/agenda")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Sort orden = Sort.by("fecha")
                .ascending()
                .and(
                        Sort.by("horaInicio").ascending()
                );

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                orden
        );

        Page<Actividad> actividades
                = actividadService.listarActividades(
                        buscar,
                        paginacion
                );

        modelo.addAttribute(
                "paginaActividades",
                actividades
        );

        modelo.addAttribute(
                "buscar",
                buscar
        );

        return "agenda/inicio";
    }

    @GetMapping("/agenda/crear")
    public String mostrarCrear(Model modelo) {

        LocalDate fecha = LocalDate.now().plusDays(1);

        Actividad actividad = new Actividad();

        actividad.setFecha(fecha);
        actividad.setTipo("INSTITUCIONAL");

        actividad.setHoraInicio(
                LocalDateTime.of(
                        fecha,
                        java.time.LocalTime.of(8, 0)
                )
        );

        actividad.setHoraFin(
                LocalDateTime.of(
                        fecha,
                        java.time.LocalTime.of(9, 0)
                )
        );

        prepararRelacionesVacias(actividad);

        modelo.addAttribute(
                "actividad",
                actividad
        );

        cargarCatalogos(modelo);

        return "agenda/crear";
    }

    @PostMapping("/agenda/crear")
    public String crearActividad(
            @Valid @ModelAttribute("actividad")
            Actividad actividad,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {

            prepararRelacionesVacias(actividad);
            cargarCatalogos(modelo);

            return "agenda/crear";
        }

        try {
            actividadService.registrarActividad(
                    actividad
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Actividad registrada correctamente"
            );

            return "redirect:/agenda";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "actividad.invalida",
                    excepcion.getMessage()
            );

            prepararRelacionesVacias(actividad);
            cargarCatalogos(modelo);

            return "agenda/crear";
        }
    }

    @GetMapping("/agenda/actualizar/{id}")
    public String mostrarActualizar(
            @PathVariable Integer id,
            Model modelo) {

        Actividad actividad
                = actividadService.buscarPorId(id);

        prepararRelacionesVacias(actividad);

        modelo.addAttribute(
                "actividad",
                actividad
        );

        cargarCatalogos(modelo);

        return "agenda/actualizar";
    }

    @PostMapping("/agenda/actualizar/{id}")
    public String actualizarActividad(
            @PathVariable Integer id,
            @Valid @ModelAttribute("actividad")
            Actividad actividad,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        actividad.setIdActividad(id);

        if (resultado.hasErrors()) {

            prepararRelacionesVacias(actividad);
            cargarCatalogos(modelo);

            return "agenda/actualizar";
        }

        try {
            actividadService.actualizarActividad(
                    id,
                    actividad
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Actividad actualizada correctamente"
            );

            return "redirect:/agenda";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "actividad.invalida",
                    excepcion.getMessage()
            );

            prepararRelacionesVacias(actividad);
            cargarCatalogos(modelo);

            return "agenda/actualizar";
        }
    }

    @GetMapping("/agenda/consultar/{id}")
    public String mostrarActividad(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "actividad",
                actividadService.buscarPorId(id)
        );

        return "agenda/consultar";
    }

    @GetMapping("/agenda/eliminar/{id}")
    public String mostrarEliminar(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "actividad",
                actividadService.buscarPorId(id)
        );

        return "agenda/eliminar";
    }

    @PostMapping("/agenda/eliminar/{id}")
    public String eliminarActividad(
            @PathVariable Integer id,
            RedirectAttributes mensaje) {

        try {
            actividadService.eliminarActividad(id);

            mensaje.addFlashAttribute(
                    "exito",
                    "Actividad eliminada correctamente"
            );

        } catch (IllegalArgumentException excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    excepcion.getMessage()
            );

        } catch (Exception excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    "No se pudo eliminar la actividad"
            );
        }

        return "redirect:/agenda";
    }

    private void cargarCatalogos(Model modelo) {

        modelo.addAttribute(
                "cursos",
                actividadService.listarCursos()
        );

        modelo.addAttribute(
                "diplomados",
                actividadService.listarDiplomados()
        );

        modelo.addAttribute(
                "alquileres",
                actividadService.listarAlquileres()
        );

        modelo.addAttribute(
                "solicitudesCatering",
                actividadService.listarSolicitudesCatering()
        );
    }

    private void prepararRelacionesVacias(
            Actividad actividad) {

        if (actividad.getCurso() == null) {
            actividad.setCurso(new Curso());
        }

        if (actividad.getDiplomado() == null) {
            actividad.setDiplomado(new Diplomado());
        }

        if (actividad.getAlquiler() == null) {
            actividad.setAlquiler(new Alquiler());
        }

        if (actividad.getSolicitudCatering() == null) {

            actividad.setSolicitudCatering(
                    new SolicitudCatering()
            );
        }
    }
    
}
