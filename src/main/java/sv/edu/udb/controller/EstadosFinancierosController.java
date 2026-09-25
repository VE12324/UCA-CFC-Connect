package sv.edu.udb.controller;


import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sv.edu.udb.service.PagoService;


@Controller
@RequiredArgsConstructor
public class EstadosFinancierosController {


    private final PagoService pagoService;


    @GetMapping("/contabilidad/estados-financieros")
    public String mostrarEstadosFinancieros(Model modelo) {
        modelo.addAttribute("totalRecaudado", pagoService.totalRecaudado());
        modelo.addAttribute("totalPendiente", pagoService.totalPendiente());


        Map<String, BigDecimal> porMetodo = new LinkedHashMap<>();
        for (Object[] fila : pagoService.desglosePorMetodo()) {
            porMetodo.put((String) fila[0], (BigDecimal) fila[1]);
        }
        modelo.addAttribute("desglosePorMetodo", porMetodo);


        Map<String, BigDecimal> porEstado = new LinkedHashMap<>();
        for (Object[] fila : pagoService.desglosePorEstado()) {
            porEstado.put((String) fila[0], (BigDecimal) fila[1]);
        }
        modelo.addAttribute("desglosePorEstado", porEstado);


        return "contabilidad/estados-financieros";
    }
}
