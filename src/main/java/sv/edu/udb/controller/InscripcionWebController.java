/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Curso;
import sv.edu.udb.model.EstadoInscripcion;
import sv.edu.udb.model.Inscripcion;
import sv.edu.udb.model.Participante;
import sv.edu.udb.service.InscripcionService;
/**
 *
 * @author crist
 */
@Controller
@RequiredArgsConstructor
public class InscripcionWebController {
    
    private final InscripcionService inscripcionService;

    @GetMapping("/inscripciones")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("fecha").descending()
        );

        Page<Inscripcion> inscripciones
                = inscripcionService.listarInscripciones(
                        buscar,
                        paginacion
                );

        modelo.addAttribute(
                "paginaInscripciones",
                inscripciones
        );

        modelo.addAttribute(
                "buscar",
                buscar
        );

        return "inscripciones/inicio";
    }

    @GetMapping("/inscripciones/crear")
    public String mostrarCrear(Model modelo) {

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setFecha(LocalDate.now());
        inscripcion.setCliente(new Cliente());
        inscripcion.setCurso(new Curso());
        inscripcion.setEstadoInscripcion(
                new EstadoInscripcion()
        );

        modelo.addAttribute(
                "inscripcion",
                inscripcion
        );

        modelo.addAttribute(
                "participantesSeleccionados",
                Set.of()
        );

        cargarCatalogos(modelo);

        return "inscripciones/crear";
    }

    @PostMapping("/inscripciones/crear")
    public String crearInscripcion(
            @Valid @ModelAttribute("inscripcion")
            Inscripcion inscripcion,
            BindingResult resultado,
            @RequestParam(
                    name = "participantesIds",
                    required = false
            )
            List<Integer> participantesIds,
            Model modelo,
            RedirectAttributes mensaje) {

        validarParticipantes(
                participantesIds,
                resultado
        );

        if (resultado.hasErrors()) {

            modelo.addAttribute(
                    "participantesSeleccionados",
                    convertirAConjunto(participantesIds)
            );

            cargarCatalogos(modelo);

            return "inscripciones/crear";
        }

        try {
            inscripcionService.registrarInscripcion(
                    inscripcion,
                    participantesIds
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Inscripción registrada correctamente"
            );

            return "redirect:/inscripciones";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "inscripcion.invalida",
                    excepcion.getMessage()
            );

            modelo.addAttribute(
                    "participantesSeleccionados",
                    convertirAConjunto(participantesIds)
            );

            cargarCatalogos(modelo);

            return "inscripciones/crear";
        }
    }

    @GetMapping("/inscripciones/actualizar/{id}")
    public String mostrarActualizar(
            @PathVariable Integer id,
            Model modelo) {

        Inscripcion inscripcion
                = inscripcionService.buscarPorId(id);

        Set<Integer> participantesSeleccionados
                = inscripcion.getParticipantes()
                        .stream()
                        .map(Participante::getIdParticipante)
                        .collect(Collectors.toSet());

        modelo.addAttribute(
                "inscripcion",
                inscripcion
        );

        modelo.addAttribute(
                "participantesSeleccionados",
                participantesSeleccionados
        );

        cargarCatalogos(modelo);

        return "inscripciones/actualizar";
    }

    @PostMapping("/inscripciones/actualizar/{id}")
    public String actualizarInscripcion(
            @PathVariable Integer id,
            @Valid @ModelAttribute("inscripcion")
            Inscripcion inscripcion,
            BindingResult resultado,
            @RequestParam(
                    name = "participantesIds",
                    required = false
            )
            List<Integer> participantesIds,
            Model modelo,
            RedirectAttributes mensaje) {

        inscripcion.setIdInscripcion(id);

        validarParticipantes(
                participantesIds,
                resultado
        );

        if (resultado.hasErrors()) {

            modelo.addAttribute(
                    "participantesSeleccionados",
                    convertirAConjunto(participantesIds)
            );

            cargarCatalogos(modelo);

            return "inscripciones/actualizar";
        }

        try {
            inscripcionService.actualizarInscripcion(
                    id,
                    inscripcion,
                    participantesIds
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Inscripción actualizada correctamente"
            );

            return "redirect:/inscripciones";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "inscripcion.invalida",
                    excepcion.getMessage()
            );

            modelo.addAttribute(
                    "participantesSeleccionados",
                    convertirAConjunto(participantesIds)
            );

            cargarCatalogos(modelo);

            return "inscripciones/actualizar";
        }
    }

    @GetMapping("/inscripciones/consultar/{id}")
    public String mostrarInscripcion(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "inscripcion",
                inscripcionService.buscarPorId(id)
        );

        return "inscripciones/consultar";
    }

    @GetMapping("/inscripciones/eliminar/{id}")
    public String mostrarEliminar(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "inscripcion",
                inscripcionService.buscarPorId(id)
        );

        return "inscripciones/eliminar";
    }

    @PostMapping("/inscripciones/eliminar/{id}")
    public String eliminarInscripcion(
            @PathVariable Integer id,
            RedirectAttributes mensaje) {

        try {
            inscripcionService.eliminarInscripcion(id);

            mensaje.addFlashAttribute(
                    "exito",
                    "Inscripción eliminada correctamente"
            );

        } catch (IllegalArgumentException excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    excepcion.getMessage()
            );

        } catch (Exception excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    "No se pudo eliminar la inscripción"
            );
        }

        return "redirect:/inscripciones";
    }

    private void cargarCatalogos(Model modelo) {

        modelo.addAttribute(
                "clientes",
                inscripcionService.listarClientes()
        );

        modelo.addAttribute(
                "cursos",
                inscripcionService.listarCursos()
        );

        modelo.addAttribute(
                "estados",
                inscripcionService.listarEstados()
        );

        modelo.addAttribute(
                "participantes",
                inscripcionService.listarParticipantes()
        );
    }

    private void validarParticipantes(
            List<Integer> participantesIds,
            BindingResult resultado) {

        if (participantesIds == null
                || participantesIds.isEmpty()) {

            resultado.reject(
                    "participantes.requeridos",
                    "Debe seleccionar al menos un participante"
            );
        }
    }

    private Set<Integer> convertirAConjunto(
            List<Integer> participantesIds) {

        if (participantesIds == null) {
            return Set.of();
        }

        return Set.copyOf(participantesIds);
    }
    
}
