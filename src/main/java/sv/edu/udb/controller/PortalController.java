package sv.edu.udb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sv.edu.udb.dto.ProgramaPortalDTO;
import sv.edu.udb.service.PortalPublicoService;

/**
 * Sitio público (solo GET): portada, oferta académica, espacios y catering.
 * "/" llega aquí por forward desde DashboardController.raiz para anónimos y
 * clientes.
 */
@Controller
@RequestMapping("/portal")
@RequiredArgsConstructor
public class PortalController implements PaginaPortalCliente {

    private final PortalPublicoService portalPublicoService;

    @GetMapping("/inicio")
    public String inicio(Model modelo) {

        portalPublicoService.buscarProximoEvento().ifPresent(evento -> {
            modelo.addAttribute("proximoEvento", evento);
            modelo.addAttribute(
                    "etiquetaEvento",
                    portalPublicoService.etiquetaEvento(evento)
            );
        });

        modelo.addAttribute(
                "destacados",
                portalPublicoService.listarDestacados(4)
        );

        return "portal/inicio";
    }

    @GetMapping("/oferta")
    public String oferta(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) Integer modalidad,
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        Page<ProgramaPortalDTO> paginaOferta = portalPublicoService.buscarOferta(
                tipo,
                categoria,
                modalidad,
                buscar,
                pagina
        );

        modelo.addAttribute("paginaOferta", paginaOferta);
        modelo.addAttribute("programas", paginaOferta.getContent());
        modelo.addAttribute("categorias", portalPublicoService.listarCategorias());
        modelo.addAttribute("modalidades", portalPublicoService.listarModalidades());
        modelo.addAttribute("tipo", tipo);
        modelo.addAttribute("categoria", categoria);
        modelo.addAttribute("modalidad", modalidad);
        modelo.addAttribute("buscar", buscar);

        return "portal/oferta";
    }

    @GetMapping("/oferta/curso/{id}")
    public String detalleCurso(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "programa",
                portalPublicoService.buscarCursoActivo(id)
        );

        return "portal/programa";
    }

    @GetMapping("/oferta/diplomado/{id}")
    public String detalleDiplomado(
            @PathVariable Integer id,
            Model modelo) {

        modelo.addAttribute(
                "programa",
                portalPublicoService.buscarDiplomadoActivo(id)
        );

        return "portal/programa";
    }

    @GetMapping("/espacios")
    public String espacios(Model modelo) {

        modelo.addAttribute(
                "espacios",
                portalPublicoService.listarEspacios()
        );

        return "portal/espacios";
    }

    @GetMapping("/catering")
    public String catering(Model modelo) {

        modelo.addAttribute(
                "servicios",
                portalPublicoService.listarServiciosCatering()
        );

        return "portal/catering";
    }
}
