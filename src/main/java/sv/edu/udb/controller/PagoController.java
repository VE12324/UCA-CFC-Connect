package sv.edu.udb.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.dto.PagoRequestDTO;
import sv.edu.udb.dto.PagoRespuestaDTO;
import sv.edu.udb.service.PagoService;


@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {


    private final PagoService pagoService;


    @GetMapping
    public ResponseEntity<Page<PagoRespuestaDTO>> listarPagos(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {


        Pageable pageable = PageRequest.of(pagina, tamano);
        return ResponseEntity.ok(pagoService.listarPagos(buscar, pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<PagoRespuestaDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.buscarPorId(id));
    }


    @PostMapping
    public ResponseEntity<PagoRespuestaDTO> registrarPago(@Valid @RequestBody PagoRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(datos));
    }


    @PutMapping("/{id}")
    public ResponseEntity<PagoRespuestaDTO> actualizarPago(@PathVariable Integer id, @Valid @RequestBody PagoRequestDTO datos) {
        return ResponseEntity.ok(pagoService.actualizarPago(id, datos));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPago(@PathVariable Integer id) {
        pagoService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/validar")
    public ResponseEntity<PagoRespuestaDTO> validarPago(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.validarPago(id));
    }


    @PostMapping("/{id}/parcial")
    public ResponseEntity<PagoRespuestaDTO> marcarComoParcial(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.marcarComoParcial(id));
    }


    @PostMapping("/{id}/comprobante")
    public ResponseEntity<PagoRespuestaDTO> emitirComprobante(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.emitirComprobante(id));
    }
}
