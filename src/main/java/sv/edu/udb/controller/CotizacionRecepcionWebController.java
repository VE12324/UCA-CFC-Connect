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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sv.edu.udb.dto.CotizacionRequestDTO;
import sv.edu.udb.dto.CotizacionRespuestaDTO;
import sv.edu.udb.dto.DetalleCotizacionItemDTO;
import sv.edu.udb.dto.DetalleCotizacionRespuestaDTO;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.service.CotizacionService;
/**
 *
 * @author crist
 */
@Controller
@RequestMapping("/cotizaciones")
@RequiredArgsConstructor
public class CotizacionRecepcionWebController {
    
      private final CotizacionService cotizacionService;
    private final ClienteRepository clienteRepository;

    @GetMapping
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10
        );

        Page<CotizacionRespuestaDTO> cotizaciones =
                cotizacionService.listarTodas(paginacion);

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

    @GetMapping("/crear")
    public String mostrarCrear(Model modelo) {

        CotizacionRequestDTO cotizacion =
                new CotizacionRequestDTO();

        cotizacion.setFecha(LocalDate.now());

        DetalleCotizacionItemDTO detalle =
                new DetalleCotizacionItemDTO();

        detalle.setCantidad(1);

        List<DetalleCotizacionItemDTO> detalles =
                new ArrayList<>();

        detalles.add(detalle);

        cotizacion.setDetalles(detalles);

        modelo.addAttribute(
                "cotizacion",
                cotizacion
        );

        cargarClientes(modelo);

        return "cotizaciones/recepcionista/crear";
    }

    @PostMapping("/crear")
    public String crearCotizacion(
            @Valid
            @ModelAttribute("cotizacion")
            CotizacionRequestDTO cotizacion,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {

            asegurarDetalle(cotizacion);
            cargarClientes(modelo);

            return "cotizaciones/recepcionista/crear";
        }

        try {

            cotizacionService.registrarCotizacion(
                    cotizacion
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Cotización registrada correctamente"
            );

            return "redirect:/cotizaciones";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "cotizacion.invalida",
                    excepcion.getMessage()
            );

            asegurarDetalle(cotizacion);
            cargarClientes(modelo);

            return "cotizaciones/recepcionista/crear";
        }
    }

    @GetMapping("/consultar/{id}")
    public String mostrarCotizacion(
            @PathVariable Integer id,
            Model modelo) {

        CotizacionRespuestaDTO cotizacion =
                cotizacionService.buscarPorId(id);

        modelo.addAttribute(
                "cotizacion",
                cotizacion
        );

        return "cotizaciones/recepcionista/consultar";
    }

    @GetMapping("/actualizar/{id}")
    public String mostrarActualizar(
            @PathVariable Integer id,
            Model modelo) {

        CotizacionRespuestaDTO respuesta =
                cotizacionService.buscarPorId(id);

        CotizacionRequestDTO cotizacion =
                convertirAFormulario(respuesta);

        modelo.addAttribute(
                "cotizacion",
                cotizacion
        );

        modelo.addAttribute(
                "idCotizacion",
                id
        );

        cargarClientes(modelo);

        return "cotizaciones/recepcionista/actualizar";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarCotizacion(
            @PathVariable Integer id,
            @Valid
            @ModelAttribute("cotizacion")
            CotizacionRequestDTO cotizacion,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {

            modelo.addAttribute(
                    "idCotizacion",
                    id
            );

            asegurarDetalle(cotizacion);
            cargarClientes(modelo);

            return "cotizaciones/recepcionista/actualizar";
        }

        try {

            cotizacionService.actualizarCotizacion(
                    id,
                    cotizacion
            );

            mensaje.addFlashAttribute(
                    "exito",
                    "Cotización actualizada correctamente"
            );

            return "redirect:/cotizaciones";

        } catch (IllegalArgumentException excepcion) {

            resultado.reject(
                    "cotizacion.invalida",
                    excepcion.getMessage()
            );

            modelo.addAttribute(
                    "idCotizacion",
                    id
            );

            asegurarDetalle(cotizacion);
            cargarClientes(modelo);

            return "cotizaciones/recepcionista/actualizar";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String mostrarEliminar(
            @PathVariable Integer id,
            Model modelo) {

        CotizacionRespuestaDTO cotizacion =
                cotizacionService.buscarPorId(id);

        modelo.addAttribute(
                "cotizacion",
                cotizacion
        );

        return "cotizaciones/recepcionista/eliminar";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCotizacion(
            @PathVariable Integer id,
            RedirectAttributes mensaje) {

        try {

            cotizacionService.eliminarCotizacion(id);

            mensaje.addFlashAttribute(
                    "exito",
                    "Cotización eliminada correctamente"
            );

        } catch (IllegalArgumentException excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    excepcion.getMessage()
            );

        } catch (Exception excepcion) {

            mensaje.addFlashAttribute(
                    "error",
                    "No se pudo eliminar la cotización"
            );
        }

        return "redirect:/cotizaciones";
    }

    private void cargarClientes(Model modelo) {

        List<Cliente> clientes =
                clienteRepository.findAll(
                        Sort.by("nombre").ascending()
                );

        modelo.addAttribute(
                "clientes",
                clientes
        );
    }

    private CotizacionRequestDTO convertirAFormulario(
            CotizacionRespuestaDTO respuesta) {

        CotizacionRequestDTO cotizacion =
                new CotizacionRequestDTO();

        cotizacion.setIdCliente(
                respuesta.idCliente()
        );

        cotizacion.setFecha(
                respuesta.fecha()
        );

        List<DetalleCotizacionItemDTO> detalles =
                new ArrayList<>();

        if (respuesta.detalles() != null) {

            for (DetalleCotizacionRespuestaDTO detalleRespuesta
                    : respuesta.detalles()) {

                DetalleCotizacionItemDTO detalle =
                        new DetalleCotizacionItemDTO();

                detalle.setDescripcion(
                        detalleRespuesta.descripcion()
                );

                detalle.setCantidad(
                        detalleRespuesta.cantidad()
                );

                detalle.setPrecio(
                        detalleRespuesta.precio()
                );

                detalles.add(detalle);
            }
        }

        if (detalles.isEmpty()) {

            DetalleCotizacionItemDTO detalle =
                    new DetalleCotizacionItemDTO();

            detalle.setCantidad(1);

            detalles.add(detalle);
        }

        cotizacion.setDetalles(detalles);

        return cotizacion;
    }

    private void asegurarDetalle(
            CotizacionRequestDTO cotizacion) {

        if (cotizacion.getDetalles() == null
                || cotizacion.getDetalles().isEmpty()) {

            DetalleCotizacionItemDTO detalle =
                    new DetalleCotizacionItemDTO();

            detalle.setCantidad(1);

            List<DetalleCotizacionItemDTO> detalles =
                    new ArrayList<>();

            detalles.add(detalle);

            cotizacion.setDetalles(detalles);
        }
    }
}
