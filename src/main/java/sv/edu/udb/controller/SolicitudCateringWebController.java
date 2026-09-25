/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
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
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.service.SolicitudCateringService;
/**
 *
 * @author crist
 */
@Controller
@RequiredArgsConstructor
public class SolicitudCateringWebController {
    
        private final SolicitudCateringService solicitudCateringService;

    @GetMapping("/solicitudes-catering")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Sort orden = Sort.by("fecha")
                .descending()
                .and(
                        Sort.by("hora").descending()
                );

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                orden
        );

        Page<SolicitudCatering> solicitudes
                = solicitudCateringService.listarSolicitudes(
                        buscar,
                        paginacion
                );

        modelo.addAttribute(
                "paginaSolicitudes",
                solicitudes
        );

        modelo.addAttribute(
                "buscar",
                buscar
        );

        return "solicitudes-catering/inicio";
    }

    @GetMapping("/solicitudes-catering/crear")
    public String mostrarCrear(Model modelo) {

        SolicitudCatering solicitud
                = new SolicitudCatering();

        solicitud.setFecha(
                LocalDate.now().plusDays(1)
        );

        solicitud.setHora(
                LocalTime.of(12, 0)
        );

        solicitud.setCantidadAsistentes(1);
        solicitud.setEstado("PENDIENTE");
        solicitud.setCliente(new Cliente());

        solicitud.setServicioCatering(
                new ServicioCatering()
        );

        modelo.addAttribute(
                "solicitud",
                solicitud
        );

        cargarCatalogos(modelo);

        return "solicitudes-catering/crear";
    }

    @PostMapping("/solicitudes-catering/crear")
    public String crearSolicitud(
            @Valid @ModelAttribute("solicitud")
            SolicitudCatering solicitud,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {

            cargarCatalogos(modelo);

            return "solicitudes-catering/crear";
        }

        try {
            solicitudCateringService.registrarSolicitud(
                    solicitud
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Solicitud de catering registrada correctamente"
            );

            return "redirect:/solicitudes-catering";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "solicitud.invalida",
                    excepcion.getMessage()
            );

            cargarCatalogos(modelo);

            return "solicitudes-catering/crear";
        }
    }

    @GetMapping("/solicitudes-catering/actualizar/{id}")
    public String mostrarActualizar(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "solicitud",
                solicitudCateringService.buscarPorId(id)
        );

        cargarCatalogos(modelo);

        return "solicitudes-catering/actualizar";
    }

    @PostMapping("/solicitudes-catering/actualizar/{id}")
    public String actualizarSolicitud(
            @PathVariable Integer id,
            @Valid @ModelAttribute("solicitud")
            SolicitudCatering solicitud,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        solicitud.setIdSolicitud(id);

        if (resultado.hasErrors()) {

            cargarCatalogos(modelo);

            return "solicitudes-catering/actualizar";
        }

        try {
            solicitudCateringService.actualizarSolicitud(
                    id,
                    solicitud
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Solicitud de catering actualizada correctamente"
            );

            return "redirect:/solicitudes-catering";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "solicitud.invalida",
                    excepcion.getMessage()
            );

            cargarCatalogos(modelo);

            return "solicitudes-catering/actualizar";
        }
    }

    @GetMapping("/solicitudes-catering/consultar/{id}")
    public String mostrarSolicitud(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "solicitud",
                solicitudCateringService.buscarPorId(id)
        );

        return "solicitudes-catering/consultar";
    }

    @GetMapping("/solicitudes-catering/eliminar/{id}")
    public String mostrarEliminar(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "solicitud",
                solicitudCateringService.buscarPorId(id)
        );

        return "solicitudes-catering/eliminar";
    }

    @PostMapping("/solicitudes-catering/eliminar/{id}")
    public String eliminarSolicitud(
            @PathVariable Integer id,
            RedirectAttributes mensaje) {

        try {
            solicitudCateringService.eliminarSolicitud(id);

            mensaje.addFlashAttribute(
                    "exito",
                    "Solicitud de catering eliminada correctamente"
            );

        } catch (IllegalArgumentException excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    excepcion.getMessage()
            );

        } catch (Exception excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    "No se pudo eliminar la solicitud de catering"
            );
        }

        return "redirect:/solicitudes-catering";
    }

    private void cargarCatalogos(Model modelo) {

        modelo.addAttribute(
                "clientes",
                solicitudCateringService.listarClientes()
        );

        modelo.addAttribute(
                "servicios",
                solicitudCateringService.listarServicios()
        );
    }
    
}
