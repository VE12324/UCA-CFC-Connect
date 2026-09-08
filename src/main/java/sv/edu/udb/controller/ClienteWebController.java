package sv.edu.udb.controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import sv.edu.udb.model.Cliente;
import sv.edu.udb.service.ClienteService;
/**
 *
 * @author crist
 */
@Controller
@RequiredArgsConstructor
public class ClienteWebController {
    
    
   private final ClienteService clienteServicio;

    @GetMapping("/clientes")
    public String mostrarInicio(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Pageable paginacion = PageRequest.of(
                pagina,
                10,
                Sort.by("nombre").ascending()
        );

        Page<Cliente> clientes =
                clienteServicio.listarClientes(
                        buscar,
                        paginacion
                );

        modelo.addAttribute("paginaClientes", clientes);
        modelo.addAttribute("buscar", buscar);

        return "clientes/inicio";
    }

    @GetMapping("/clientes/crear")
    public String mostrarCrear(Model modelo) {

        modelo.addAttribute("cliente", new Cliente());

        return "clientes/crear";
    }

    @PostMapping("/clientes/crear")
    public String crearCliente(
            @Valid @ModelAttribute("cliente") Cliente cliente,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "clientes/crear";
        }

        try {
            clienteServicio.registrarCliente(cliente);

            mensaje.addFlashAttribute(
                    "exito",
                    "Cliente registrado correctamente"
            );

            return "redirect:/clientes";

        } catch (IllegalArgumentException excepcion) {

            resultado.rejectValue(
                    "dui",
                    "dui.repetido",
                    excepcion.getMessage()
            );

            return "clientes/crear";
        }
    }

    @GetMapping("/clientes/consultar/{id}")
    public String mostrarCliente(
            @PathVariable Integer id,
            Model modelo) {

        Cliente cliente = clienteServicio.buscarPorId(id);

        modelo.addAttribute("cliente", cliente);

        return "clientes/consultar";
    }

    @GetMapping("/clientes/actualizar/{id}")
    public String mostrarActualizar(
            @PathVariable Integer id,
            Model modelo) {

        Cliente cliente = clienteServicio.buscarPorId(id);

        modelo.addAttribute("cliente", cliente);

        return "clientes/actualizar";
    }

    @PostMapping("/clientes/actualizar/{id}")
    public String actualizarCliente(
            @PathVariable Integer id,
            @Valid @ModelAttribute("cliente") Cliente cliente,
            BindingResult resultado,
            RedirectAttributes mensaje) {

        if (resultado.hasErrors()) {
            return "clientes/actualizar";
        }

        try {
            clienteServicio.actualizarCliente(id, cliente);

            mensaje.addFlashAttribute(
                    "exito",
                    "Cliente actualizado correctamente"
            );

            return "redirect:/clientes";

        } catch (IllegalArgumentException excepcion) {

            resultado.rejectValue(
                    "dui",
                    "dui.repetido",
                    excepcion.getMessage()
            );

            return "clientes/actualizar";
        }
    }

    @GetMapping("/clientes/eliminar/{id}")
    public String mostrarEliminar(
            @PathVariable Integer id,
            Model modelo) {

        Cliente cliente = clienteServicio.buscarPorId(id);

        modelo.addAttribute("cliente", cliente);

        return "clientes/eliminar";
    }

    @PostMapping("/clientes/eliminar/{id}")
    public String eliminarCliente(
            @PathVariable Integer id,
            RedirectAttributes mensaje) {

        clienteServicio.eliminarCliente(id);

        mensaje.addFlashAttribute(
                "exito",
                "Cliente eliminado correctamente"
        );

        return "redirect:/clientes";
    }
}
