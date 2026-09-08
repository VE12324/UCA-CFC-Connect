/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import sv.edu.udb.model.Participante;
import sv.edu.udb.service.ParticipanteService;

/**
 *
 * @author crist
 */
@Controller
@RequiredArgsConstructor
public class ParticipanteControlllerWeb {
    
    
    private final ParticipanteService participanteServicio;

    @GetMapping("/participantes")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("nombre").ascending()
        );

        Page<Participante> participantes =
                participanteServicio.listarParticipantes(
                        buscar,
                        paginacion
                );

        modelo.addAttribute(
                "paginaParticipantes",
                participantes
        );

        modelo.addAttribute("buscar", buscar);

        return "participantes/inicio";
    }

    @GetMapping("/participantes/crear")
    public String mostrarCrear(Model modelo) {

        modelo.addAttribute(
                "participante",
                new Participante()
        );

        return "participantes/crear";
    }

    @PostMapping("/participantes/crear")
    public String crearParticipante(
            @Valid
            @ModelAttribute("participante")
            Participante participante,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "participantes/crear";
        }

        try {
            participanteServicio.registrarParticipante(
                    participante
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Participante registrado correctamente"
            );

            return "redirect:/participantes";

        } catch (IllegalArgumentException excepcion) {

            resultado.rejectValue(
                    "dui",
                    "dui.repetido",
                    excepcion.getMessage()
            );

            return "participantes/crear";
        }
    }

    @GetMapping("/participantes/consultar/{id}")
    public String mostrarParticipante(
            @PathVariable Integer id,
            Model modelo) {

        Participante participante =
                participanteServicio.buscarPorId(id);

        modelo.addAttribute(
                "participante",
                participante
        );

        return "participantes/consultar";
    }

    @GetMapping("/participantes/actualizar/{id}")
    public String mostrarActualizar(
            @PathVariable Integer id,
            Model modelo) {

        Participante participante =
                participanteServicio.buscarPorId(id);

        modelo.addAttribute(
                "participante",
                participante
        );

        return "participantes/actualizar";
    }

    @PostMapping("/participantes/actualizar/{id}")
    public String actualizarParticipante(
            @PathVariable Integer id,
            @Valid
            @ModelAttribute("participante")
            Participante participante,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "participantes/actualizar";
        }

        try {
            participanteServicio.actualizarParticipante(
                    id,
                    participante
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Participante actualizado correctamente"
            );

            return "redirect:/participantes";

        } catch (IllegalArgumentException excepcion) {

            resultado.rejectValue(
                    "dui",
                    "dui.repetido",
                    excepcion.getMessage()
            );

            return "participantes/actualizar";
        }
    }

    @GetMapping("/participantes/eliminar/{id}")
    public String mostrarEliminar(
            @PathVariable Integer id,
            Model modelo) {

        Participante participante =
                participanteServicio.buscarPorId(id);

        modelo.addAttribute(
                "participante",
                participante
        );

        return "participantes/eliminar";
    }

    @PostMapping("/participantes/eliminar/{id}")
    public String eliminarParticipante(
            @PathVariable Integer id,
            RedirectAttributes mensaje) {

        participanteServicio.eliminarParticipante(id);

        mensaje.addFlashAttribute(
                "exito",
                "Participante eliminado correctamente"
        );

        return "redirect:/participantes";
    } 
}
