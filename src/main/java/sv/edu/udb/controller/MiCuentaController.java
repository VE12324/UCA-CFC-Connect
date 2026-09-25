/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.controller;

import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sv.edu.udb.dto.CotizacionRespuestaDTO;
import sv.edu.udb.dto.PagoRespuestaDTO;
import sv.edu.udb.model.Actividad;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Inscripcion;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.service.ClienteService;
import sv.edu.udb.service.CotizacionService;
import sv.edu.udb.service.PagoService;
import sv.edu.udb.service.PortalClienteService;
/**
 *
 * @author crist
 */
@Controller
@RequestMapping("/mi-cuenta")
@RequiredArgsConstructor
public class MiCuentaController {
    
    private final ClienteService clienteService;
    private final CotizacionService cotizacionService;
    private final PagoService pagoService;
    private final PortalClienteService portalClienteService;

    @GetMapping
    public String mostrarInicio(
            Principal principal,
            Model modelo) {

        modelo.addAttribute(
                "cliente",
                obtenerCliente(principal)
        );

        return "mi-cuenta/inicio";
    }

    @GetMapping("/inscripciones")
    public String mostrarInscripciones(
            @RequestParam(defaultValue = "0") int pagina,
            Principal principal,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("fecha").descending()
        );

        Page<Inscripcion> paginaInscripciones =
                portalClienteService.listarInscripciones(
                        obtenerCorreo(principal),
                        paginacion
                );

        modelo.addAttribute(
                "paginaInscripciones",
                paginaInscripciones
        );

        modelo.addAttribute(
                "inscripciones",
                paginaInscripciones.getContent()
        );

        modelo.addAttribute(
                "cliente",
                obtenerCliente(principal)
        );

        return "mi-cuenta/inscripciones";
    }

    @GetMapping("/cotizaciones")
    public String mostrarCotizaciones(
            @RequestParam(defaultValue = "0") int pagina,
            Principal principal,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("fecha").descending()
        );

        Page<CotizacionRespuestaDTO> paginaCotizaciones =
                cotizacionService.listarPorCorreoCliente(
                        obtenerCorreo(principal),
                        paginacion
                );

        modelo.addAttribute(
                "paginaCotizaciones",
                paginaCotizaciones
        );

        modelo.addAttribute(
                "cotizaciones",
                paginaCotizaciones.getContent()
        );

        modelo.addAttribute(
                "cliente",
                obtenerCliente(principal)
        );

        return "mi-cuenta/cotizaciones";
    }

    @GetMapping("/alquileres")
    public String mostrarAlquileres(
            @RequestParam(defaultValue = "0") int pagina,
            Principal principal,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("horaInicio").descending()
        );

        Page<Alquiler> paginaAlquileres =
                portalClienteService.listarAlquileres(
                        obtenerCorreo(principal),
                        paginacion
                );

        modelo.addAttribute(
                "paginaAlquileres",
                paginaAlquileres
        );

        modelo.addAttribute(
                "alquileres",
                paginaAlquileres.getContent()
        );

        modelo.addAttribute(
                "cliente",
                obtenerCliente(principal)
        );

        return "mi-cuenta/alquileres";
    }

    @GetMapping("/catering")
    public String mostrarCatering(
            @RequestParam(defaultValue = "0") int pagina,
            Principal principal,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("fecha")
                        .descending()
                        .and(
                                Sort.by("hora").descending()
                        )
        );

        Page<SolicitudCatering> paginaCatering =
                portalClienteService.listarCatering(
                        obtenerCorreo(principal),
                        paginacion
                );

        modelo.addAttribute(
                "paginaCatering",
                paginaCatering
        );

        modelo.addAttribute(
                "solicitudes",
                paginaCatering.getContent()
        );

        modelo.addAttribute(
                "cliente",
                obtenerCliente(principal)
        );

        return "mi-cuenta/catering";
    }

    @GetMapping("/pagos")
    public String mostrarPagos(
            @RequestParam(defaultValue = "0") int pagina,
            Principal principal,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("fecha").descending()
        );

        Page<PagoRespuestaDTO> paginaPagos =
                pagoService.listarPorCorreoCliente(
                        obtenerCorreo(principal),
                        paginacion
                );

        modelo.addAttribute(
                "paginaPagos",
                paginaPagos
        );

        modelo.addAttribute(
                "pagos",
                paginaPagos.getContent()
        );

        modelo.addAttribute(
                "cliente",
                obtenerCliente(principal)
        );

        return "mi-cuenta/pagos";
    }

    @GetMapping("/agenda")
    public String mostrarAgenda(
            Principal principal,
            Model modelo) {

        List<Actividad> actividades =
                portalClienteService.listarAgenda(
                        obtenerCorreo(principal)
                );

        modelo.addAttribute(
                "actividades",
                actividades
        );

        modelo.addAttribute(
                "cliente",
                obtenerCliente(principal)
        );

        return "mi-cuenta/agenda";
    }

    private Cliente obtenerCliente(
            Principal principal) {

        return clienteService.buscarPorCorreoUsuario(
                obtenerCorreo(principal)
        );
    }

    private String obtenerCorreo(
            Principal principal) {

        if (principal == null
                || principal.getName() == null
                || principal.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "No se pudo identificar al usuario autenticado."
            );
        }

        return principal.getName();
    }
}
