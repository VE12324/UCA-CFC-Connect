/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import sv.edu.udb.dto.CotizacionRequestDTO;
import sv.edu.udb.dto.CotizacionRespuestaDTO;
import sv.edu.udb.dto.DetalleCotizacionItemDTO;
import sv.edu.udb.dto.DetalleCotizacionRespuestaDTO;
import sv.edu.udb.service.CotizacionService;
/**
 *
 * @author crist
 */
@Controller
@RequiredArgsConstructor
public class CotizacionRecepcionWebController {
    
     private final CotizacionService cotizacionService;

    @GetMapping("/cotizaciones")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("fecha")
                        .descending()
        );

        Page<CotizacionRespuestaDTO> cotizaciones =
                cotizacionService.listarCotizaciones(
                        buscar,
                        paginacion
                );

        modelo.addAttribute(
                "paginaCotizaciones",
                cotizaciones
        );

        modelo.addAttribute(
                "buscar",
                buscar
        );

        return "cotizaciones/recepcionista/inicio";
    }

    @GetMapping("/cotizaciones/crear")
    public String mostrarCrear(
            Model modelo) {

        CotizacionRequestDTO cotizacion =
                new CotizacionRequestDTO();

        cotizacion.setFecha(
                LocalDate.now()
        );

        List<DetalleCotizacionItemDTO> detalles =
                new ArrayList<>();

        detalles.add(
                new DetalleCotizacionItemDTO()
        );

        cotizacion.setDetalles(
                detalles
        );

        modelo.addAttribute(
                "cotizacion",
                cotizacion
        );

        cargarClientes(
                modelo
        );

        return "cotizaciones/recepcionista/crear";
    }

    @PostMapping("/cotizaciones/crear")
    public String crearCotizacion(
            @Valid
            @ModelAttribute("cotizacion")
            CotizacionRequestDTO cotizacion,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        eliminarDetallesVacios(
                cotizacion
        );

        if (cotizacion.getDetalles() == null
                || cotizacion.getDetalles().isEmpty()) {

            resultado.reject(
                    "detalles.requeridos",
                    "Debe agregar al menos un detalle."
            );
        }

        if (resultado.hasErrors()) {

            garantizarDetalle(
                    cotizacion
            );

            cargarClientes(
                    modelo
            );

            return "cotizaciones/recepcionista/crear";
        }

        try {

            cotizacionService.registrarCotizacion(
                    cotizacion
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Cotización registrada correctamente."
            );

            return "redirect:/cotizaciones";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "cotizacion.invalida",
                    excepcion.getMessage()
            );

            garantizarDetalle(
                    cotizacion
            );

            cargarClientes(
                    modelo
            );

            return "cotizaciones/recepcionista/crear";
        }
    }

    @GetMapping("/cotizaciones/consultar/{id}")
    public String mostrarCotizacion(
            @PathVariable Integer id,
            Model modelo) {

        CotizacionRespuestaDTO cotizacion =
                cotizacionService.buscarPorId(
                        id
                );

        modelo.addAttribute(
                "cotizacion",
                cotizacion
        );

        return "cotizaciones/recepcionista/consultar";
    }

    @GetMapping("/cotizaciones/actualizar/{id}")
    public String mostrarActualizar(
            @PathVariable Integer id,
            Model modelo) {

        CotizacionRespuestaDTO respuesta =
                cotizacionService.buscarPorId(
                        id
                );

        CotizacionRequestDTO cotizacion =
                convertirAFormulario(
                        respuesta
                );

        modelo.addAttribute(
                "idCotizacion",
                id
        );

        modelo.addAttribute(
                "estadoCotizacion",
                respuesta.estado()
        );

        modelo.addAttribute(
                "cotizacion",
                cotizacion
        );

        cargarClientes(
                modelo
        );

        return "cotizaciones/recepcionista/actualizar";
    }

    @PostMapping("/cotizaciones/actualizar/{id}")
    public String actualizarCotizacion(
            @PathVariable Integer id,
            @Valid
            @ModelAttribute("cotizacion")
            CotizacionRequestDTO cotizacion,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        eliminarDetallesVacios(
                cotizacion
        );

        if (cotizacion.getDetalles() == null
                || cotizacion.getDetalles().isEmpty()) {

            resultado.reject(
                    "detalles.requeridos",
                    "Debe agregar al menos un detalle."
            );
        }

        if (resultado.hasErrors()) {

            prepararActualizacionConError(
                    id,
                    cotizacion,
                    modelo
            );

            return "cotizaciones/recepcionista/actualizar";
        }

        try {

            cotizacionService.actualizarCotizacion(
                    id,
                    cotizacion
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Cotización actualizada correctamente."
            );

            return "redirect:/cotizaciones";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "cotizacion.invalida",
                    excepcion.getMessage()
            );

            prepararActualizacionConError(
                    id,
                    cotizacion,
                    modelo
            );

            return "cotizaciones/recepcionista/actualizar";
        }
    }

    @GetMapping("/cotizaciones/eliminar/{id}")
    public String mostrarEliminar(
            @PathVariable Integer id,
            Model modelo) {

        CotizacionRespuestaDTO cotizacion =
                cotizacionService.buscarPorId(
                        id
                );

        modelo.addAttribute(
                "cotizacion",
                cotizacion
        );

        return "cotizaciones/recepcionista/eliminar";
    }

    @PostMapping("/cotizaciones/eliminar/{id}")
    public String eliminarCotizacion(
            @PathVariable Integer id,
            RedirectAttributes mensaje) {

        try {

            cotizacionService.eliminarCotizacion(
                    id
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Cotización eliminada correctamente."
            );

        } catch (IllegalArgumentException excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    excepcion.getMessage()
            );

        } catch (Exception excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    "No se pudo eliminar la cotización."
            );
        }

        return "redirect:/cotizaciones";
    }

    private void cargarClientes(
            Model modelo) {

        modelo.addAttribute(
                "clientes",
                cotizacionService.listarClientes()
        );
    }

    private CotizacionRequestDTO convertirAFormulario(
            CotizacionRespuestaDTO respuesta) {

        CotizacionRequestDTO formulario =
                new CotizacionRequestDTO();

        formulario.setIdCliente(
                respuesta.idCliente()
        );

        formulario.setFecha(
                respuesta.fecha()
        );

        List<DetalleCotizacionItemDTO> detalles =
                new ArrayList<>();

        for (DetalleCotizacionRespuestaDTO detalle
                : respuesta.detalles()) {

            DetalleCotizacionItemDTO item =
                    new DetalleCotizacionItemDTO();

            item.setDescripcion(
                    detalle.descripcion()
            );

            item.setCantidad(
                    detalle.cantidad()
            );

            item.setPrecio(
                    detalle.precio()
            );

            detalles.add(
                    item
            );
        }

        formulario.setDetalles(
                detalles
        );

        garantizarDetalle(
                formulario
        );

        return formulario;
    }

    private void prepararActualizacionConError(
            Integer id,
            CotizacionRequestDTO cotizacion,
            Model modelo) {

        garantizarDetalle(
                cotizacion
        );

        modelo.addAttribute(
                "idCotizacion",
                id
        );

        try {

            CotizacionRespuestaDTO respuesta =
                    cotizacionService.buscarPorId(
                            id
                    );

            modelo.addAttribute(
                    "estadoCotizacion",
                    respuesta.estado()
            );

        } catch (Exception excepcion) {

            modelo.addAttribute(
                    "estadoCotizacion",
                    ""
            );
        }

        cargarClientes(
                modelo
        );
    }

    private void garantizarDetalle(
            CotizacionRequestDTO cotizacion) {

        if (cotizacion.getDetalles() == null) {

            cotizacion.setDetalles(
                    new ArrayList<>()
            );
        }

        if (cotizacion.getDetalles().isEmpty()) {

            cotizacion.getDetalles().add(
                    new DetalleCotizacionItemDTO()
            );
        }
    }

    private void eliminarDetallesVacios(
            CotizacionRequestDTO cotizacion) {

        if (cotizacion.getDetalles() == null) {
            return;
        }

        cotizacion.getDetalles().removeIf(
                detalle ->
                        detalle == null
                        || (
                                detalle.getDescripcion() == null
                                || detalle.getDescripcion().isBlank()
                        )
                        && detalle.getCantidad() == null
                        && detalle.getPrecio() == null
        );
    }
    
}
