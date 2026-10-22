package sv.edu.udb.controller;




import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sv.edu.udb.dto.ConceptoPagoDTO;
import sv.edu.udb.dto.PagoRequestDTO;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.MetodoPagoRepository;
import sv.edu.udb.service.ConceptoPagoService;
import sv.edu.udb.service.PagoService;




import java.time.LocalDate;
import java.util.List;




@Controller
@RequiredArgsConstructor
public class PagoWebController {




    private final PagoService pagoService;
    private final ConceptoPagoService conceptoPagoService;
    private final ClienteRepository clienteRepository;
    private final MetodoPagoRepository metodoPagoRepository;




    private void cargarCatalogos(Model modelo) {
        modelo.addAttribute("clientes", clienteRepository.findAll());
        modelo.addAttribute("metodosPago", metodoPagoRepository.findAll());
    }




    // Datos de solo lectura del formulario de actualizar: cliente y concepto
    // salen del pago guardado, no de lo que envíe el navegador.
    private void cargarDatosActualizar(Integer id, Model modelo) {
        var pagoActual = pagoService.buscarPorId(id);
        ConceptoPagoDTO concepto = null;


        if (pagoActual.tipoPago() != null && pagoActual.idReferencia() != null) {
            try {
                concepto = conceptoPagoService.obtener(pagoActual.tipoPago(), pagoActual.idReferencia(), id);
            } catch (IllegalArgumentException excepcion) {
                concepto = null;
            }
        }


        modelo.addAttribute("idPago", id);
        modelo.addAttribute("nombreCliente", pagoActual.nombreCliente());
        modelo.addAttribute("concepto", concepto);
        modelo.addAttribute("metodosPago", metodoPagoRepository.findAll());
    }




    // Lista de lo que el cliente tiene pendiente de pagar, para el formulario
    // de registrar pago (los formularios web no pueden llamar a /api/**).
    @GetMapping("/contabilidad/pagos/conceptos-pendientes/{idCliente}")
    @ResponseBody
    public List<ConceptoPagoDTO> conceptosPendientes(@PathVariable Integer idCliente) {
        return conceptoPagoService.listarPendientes(idCliente);
    }




    @GetMapping("/contabilidad/pagos")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {




        Pageable paginacion = PageRequest.of(pagina, 10);




        modelo.addAttribute("paginaPagos", pagoService.listarPagos(buscar, paginacion));
        modelo.addAttribute("buscar", buscar);




        return "contabilidad/pagos/inicio";
    }




    @GetMapping("/contabilidad/pagos/crear")
    public String mostrarCrear(Model modelo) {
        PagoRequestDTO pago = new PagoRequestDTO();
        pago.setFecha(LocalDate.now());
        modelo.addAttribute("pago", pago);
        cargarCatalogos(modelo);
        return "contabilidad/pagos/crear";
    }




    @PostMapping("/contabilidad/pagos/crear")
    public String crearPago(
            @Valid @ModelAttribute("pago") PagoRequestDTO pago,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {




        if (resultado.hasErrors()) {
            cargarCatalogos(modelo);
            return "contabilidad/pagos/crear";
        }




        try {
            pagoService.registrarPago(pago);
            mensaje.addFlashAttribute("exito", "Pago registrado correctamente");
            return "redirect:/contabilidad/pagos";
        } catch (IllegalArgumentException excepcion) {
            resultado.reject("pago.invalido", excepcion.getMessage());
            cargarCatalogos(modelo);
            return "contabilidad/pagos/crear";
        }
    }




    @GetMapping("/contabilidad/pagos/actualizar/{id}")
    public String mostrarActualizar(@PathVariable Integer id, Model modelo) {
        var pagoActual = pagoService.buscarPorId(id);




        PagoRequestDTO pago = new PagoRequestDTO();
        pago.setIdCliente(pagoActual.idCliente());
        pago.setIdMetodoPago(pagoActual.idMetodoPago());
        pago.setFecha(pagoActual.fecha());
        pago.setMonto(pagoActual.monto());
        pago.setTipoPago(pagoActual.tipoPago());
        pago.setIdReferencia(pagoActual.idReferencia());




        modelo.addAttribute("pago", pago);
        cargarDatosActualizar(id, modelo);
        return "contabilidad/pagos/actualizar";
    }




    @PostMapping("/contabilidad/pagos/actualizar/{id}")
    public String actualizarPago(
            @PathVariable Integer id,
            @Valid @ModelAttribute("pago") PagoRequestDTO pago,
            BindingResult resultado,
            Model modelo,
            RedirectAttributes mensaje) {




        if (resultado.hasErrors()) {
            cargarDatosActualizar(id, modelo);
            return "contabilidad/pagos/actualizar";
        }




        try {
            pagoService.actualizarPago(id, pago);
            mensaje.addFlashAttribute("exito", "Pago actualizado correctamente");
            return "redirect:/contabilidad/pagos";
        } catch (IllegalArgumentException excepcion) {
            resultado.reject("pago.invalido", excepcion.getMessage());
            cargarDatosActualizar(id, modelo);
            return "contabilidad/pagos/actualizar";
        }
    }




    @GetMapping("/contabilidad/pagos/consultar/{id}")
    public String mostrarPago(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("pago", pagoService.buscarPorId(id));
        return "contabilidad/pagos/consultar";
    }




    @GetMapping("/contabilidad/pagos/eliminar/{id}")
    public String mostrarEliminar(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("pago", pagoService.buscarPorId(id));
        return "contabilidad/pagos/eliminar";
    }




    @PostMapping("/contabilidad/pagos/eliminar/{id}")
    public String eliminarPago(@PathVariable Integer id, RedirectAttributes mensaje) {
        try {
            pagoService.eliminarPago(id);
            mensaje.addFlashAttribute("exito", "Pago eliminado correctamente");
        } catch (IllegalArgumentException excepcion) {
            mensaje.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/contabilidad/pagos";
    }




    @PostMapping("/contabilidad/pagos/{id}/validar")
    public String validarPago(@PathVariable Integer id, RedirectAttributes mensaje) {
        try {
            var pago = pagoService.validarPago(id);
            mensaje.addFlashAttribute("exito", "Pago validado correctamente. Estado actual: " + pago.estado());
        } catch (IllegalArgumentException excepcion) {
            mensaje.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/contabilidad/pagos";
    }




    @PostMapping("/contabilidad/pagos/{id}/parcial")
    public String marcarComoParcial(@PathVariable Integer id, RedirectAttributes mensaje) {
        try {
            pagoService.marcarComoParcial(id);
            mensaje.addFlashAttribute("exito", "Pago marcado como Parcial correctamente");
        } catch (IllegalArgumentException excepcion) {
            mensaje.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/contabilidad/pagos";
    }




    @GetMapping("/contabilidad/pagos/{id}/comprobante")
    public String mostrarComprobante(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("pago", pagoService.buscarPorId(id));
        return "contabilidad/pagos/comprobante";
    }




    @PostMapping("/contabilidad/pagos/{id}/comprobante")
    public String emitirComprobante(@PathVariable Integer id, RedirectAttributes mensaje) {
        pagoService.emitirComprobante(id);
        mensaje.addFlashAttribute("exito", "Comprobante emitido correctamente");
        return "redirect:/contabilidad/pagos/" + id + "/comprobante";
    }
}


