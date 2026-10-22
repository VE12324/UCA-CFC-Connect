package sv.edu.udb.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sv.edu.udb.dto.LineaCotizacionClienteDTO;
import sv.edu.udb.dto.SolicitudAlquilerDTO;
import sv.edu.udb.dto.SolicitudCateringClienteDTO;
import sv.edu.udb.dto.SolicitudCotizacionDTO;
import sv.edu.udb.exception.CampoInvalidoException;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.service.CotizacionService;
import sv.edu.udb.service.PortalClienteService;
import sv.edu.udb.service.PortalPublicoService;
import sv.edu.udb.service.SolicitudClienteService;

@Controller
@RequestMapping("/mi-cuenta")
@RequiredArgsConstructor
public class SolicitudClienteWebController implements PaginaPortalCliente {

    private static final String MENSAJE_EXITO =
            "Tu solicitud fue enviada. Quedó en estado Pendiente "
                    + "y el equipo del centro la confirmará.";

    private final SolicitudClienteService solicitudClienteService;
    private final PortalPublicoService portalPublicoService;
    private final CotizacionService cotizacionService;
    private final PortalClienteService portalClienteService;

    // ------------------------------------------------------------------
    // Alquiler de espacios
    // ------------------------------------------------------------------

    @GetMapping("/alquileres/solicitar")
    public String formularioAlquiler(
            @RequestParam(name = "espacio", required = false) Integer idEspacio,
            Model modelo) {

        if (idEspacio == null) {
            return "redirect:/portal/espacios";
        }

        modelo.addAttribute("solicitud", new SolicitudAlquilerDTO());
        prepararAlquiler(idEspacio, modelo);

        return "mi-cuenta/solicitar-alquiler";
    }

    @PostMapping("/alquileres/solicitar")
    public String solicitarAlquiler(
            @RequestParam(name = "espacio") Integer idEspacio,
            @Valid @ModelAttribute("solicitud") SolicitudAlquilerDTO solicitud,
            BindingResult resultado,
            Authentication authentication,
            Model modelo,
            RedirectAttributes redirectAttributes) {

        if (!resultado.hasErrors()) {
            try {
                solicitudClienteService.solicitarAlquiler(
                        authentication.getName(),
                        idEspacio,
                        solicitud
                );

                redirectAttributes.addFlashAttribute("mensajeExito", MENSAJE_EXITO);

                return "redirect:/mi-cuenta/solicitudes";

            } catch (CampoInvalidoException excepcion) {
                resultado.rejectValue(excepcion.getCampo(), "invalido", excepcion.getMessage());

            } catch (IllegalArgumentException excepcion) {
                // Incluye EspacioOcupadoException (choque de horario).
                resultado.reject("solicitud", excepcion.getMessage());
            }
        }

        prepararAlquiler(idEspacio, modelo);

        return "mi-cuenta/solicitar-alquiler";
    }

    private void prepararAlquiler(Integer idEspacio, Model modelo) {

        Espacio espacio = solicitudClienteService.buscarEspacio(idEspacio);

        modelo.addAttribute("espacio", espacio);
        modelo.addAttribute("reservable", PortalPublicoService.espacioReservable(espacio));
        modelo.addAttribute(
                "reservas",
                solicitudClienteService.listarProximasReservas(idEspacio)
        );
    }

    // ------------------------------------------------------------------
    // Catering
    // ------------------------------------------------------------------

    @GetMapping("/catering/solicitar")
    public String formularioCatering(
            @RequestParam(name = "servicio", required = false) Integer idServicio,
            Model modelo) {

        if (idServicio == null) {
            return "redirect:/portal/catering";
        }

        ServicioCatering servicio = prepararCatering(idServicio, modelo);

        SolicitudCateringClienteDTO solicitud = new SolicitudCateringClienteDTO();
        solicitud.setMenu(servicio.getNombre());
        solicitud.setCantidadAsistentes(servicio.getCapacidadMinima());

        modelo.addAttribute("solicitud", solicitud);

        return "mi-cuenta/solicitar-catering";
    }

    @PostMapping("/catering/solicitar")
    public String solicitarCatering(
            @RequestParam(name = "servicio") Integer idServicio,
            @Valid @ModelAttribute("solicitud") SolicitudCateringClienteDTO solicitud,
            BindingResult resultado,
            Authentication authentication,
            Model modelo,
            RedirectAttributes redirectAttributes) {

        if (!resultado.hasErrors()) {
            try {
                solicitudClienteService.solicitarCatering(
                        authentication.getName(),
                        idServicio,
                        solicitud
                );

                redirectAttributes.addFlashAttribute("mensajeExito", MENSAJE_EXITO);

                return "redirect:/mi-cuenta/solicitudes";

            } catch (CampoInvalidoException excepcion) {
                resultado.rejectValue(excepcion.getCampo(), "invalido", excepcion.getMessage());

            } catch (IllegalArgumentException excepcion) {
                resultado.reject("solicitud", excepcion.getMessage());
            }
        }

        prepararCatering(idServicio, modelo);

        return "mi-cuenta/solicitar-catering";
    }

    private ServicioCatering prepararCatering(Integer idServicio, Model modelo) {

        ServicioCatering servicio = solicitudClienteService.buscarServicioCatering(idServicio);

        modelo.addAttribute("servicio", servicio);
        modelo.addAttribute("disponible", PortalPublicoService.cateringDisponible(servicio));
        modelo.addAttribute(
                "nombresEspacios",
                portalPublicoService.listarEspacios().stream()
                        .map(Espacio::getNombre)
                        .toList()
        );

        return servicio;
    }

    // ------------------------------------------------------------------
    // Cotización
    // ------------------------------------------------------------------

    @GetMapping("/cotizaciones/solicitar")
    public String formularioCotizacion(
            @RequestParam(required = false) Integer curso,
            @RequestParam(required = false) Integer diplomado,
            @RequestParam(required = false) Integer espacio,
            @RequestParam(required = false) Integer catering,
            @RequestParam(required = false) String empresarial,
            Model modelo) {

        SolicitudCotizacionDTO solicitud = new SolicitudCotizacionDTO();
        List<LineaCotizacionClienteDTO> lineas = solicitud.getLineas();

        if (curso != null) {
            lineas.add(new LineaCotizacionClienteDTO(
                    SolicitudClienteService.TIPO_CURSO, curso, 1, null));
        }

        if (diplomado != null) {
            lineas.add(new LineaCotizacionClienteDTO(
                    SolicitudClienteService.TIPO_DIPLOMADO, diplomado, 1, null));
        }

        if (espacio != null) {
            lineas.add(new LineaCotizacionClienteDTO(
                    SolicitudClienteService.TIPO_ESPACIO, espacio, 1, null));
        }

        if (catering != null) {
            lineas.add(new LineaCotizacionClienteDTO(
                    SolicitudClienteService.TIPO_CATERING, catering, 1, null));
        }

        if (empresarial != null) {
            lineas.add(new LineaCotizacionClienteDTO(
                    SolicitudClienteService.TIPO_OTRO, null, 1,
                    "Curso empresarial: "));
        }

        if (lineas.isEmpty()) {
            lineas.add(new LineaCotizacionClienteDTO());
        }

        modelo.addAttribute("solicitud", solicitud);
        prepararCotizacion(modelo);

        return "mi-cuenta/solicitar-cotizacion";
    }

    @PostMapping("/cotizaciones/solicitar")
    public String solicitarCotizacion(
            @ModelAttribute("solicitud") SolicitudCotizacionDTO solicitud,
            BindingResult resultado,
            Authentication authentication,
            Model modelo,
            RedirectAttributes redirectAttributes) {

        try {
            solicitudClienteService.solicitarCotizacion(
                    authentication.getName(),
                    solicitud
            );

            redirectAttributes.addFlashAttribute("mensajeExito", MENSAJE_EXITO);

            return "redirect:/mi-cuenta/solicitudes";

        } catch (IllegalArgumentException excepcion) {
            resultado.reject("solicitud", excepcion.getMessage());
        }

        if (solicitud.getLineas().isEmpty()) {
            solicitud.getLineas().add(new LineaCotizacionClienteDTO());
        }

        prepararCotizacion(modelo);

        return "mi-cuenta/solicitar-cotizacion";
    }

    private void prepararCotizacion(Model modelo) {

        modelo.addAttribute("programas", portalPublicoService.listarProgramasActivos());
        modelo.addAttribute(
                "espaciosCotizables",
                portalPublicoService.listarEspacios().stream()
                        .filter(PortalPublicoService::espacioReservable)
                        .toList()
        );
        modelo.addAttribute(
                "serviciosCotizables",
                portalPublicoService.listarServiciosCatering().stream()
                        .filter(PortalPublicoService::cateringDisponible)
                        .toList()
        );
    }

    // ------------------------------------------------------------------
    // Estado de solicitudes
    // ------------------------------------------------------------------

    @GetMapping("/solicitudes")
    public String solicitudes(
            Authentication authentication,
            Model modelo) {

        String correo = authentication.getName();

        modelo.addAttribute(
                "cotizaciones",
                cotizacionService.listarPorCorreoCliente(
                        correo,
                        PageRequest.of(0, 10, Sort.by("fecha").descending()
                                .and(Sort.by("idCotizacion").descending()))
                ).getContent()
        );

        modelo.addAttribute(
                "alquileres",
                portalClienteService.listarAlquileres(
                        correo,
                        PageRequest.of(0, 10, Sort.by("horaInicio").descending())
                ).getContent()
        );

        modelo.addAttribute(
                "solicitudesCatering",
                portalClienteService.listarCatering(
                        correo,
                        PageRequest.of(0, 10, Sort.by("fecha").descending()
                                .and(Sort.by("hora").descending()))
                ).getContent()
        );

        return "mi-cuenta/solicitudes";
    }
}
