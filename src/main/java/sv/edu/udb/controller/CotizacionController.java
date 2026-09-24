package sv.edu.udb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.dto.CotizacionRequestDTO;
import sv.edu.udb.dto.CotizacionRespuestaDTO;
import sv.edu.udb.service.CotizacionService;


@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {


    private final CotizacionService cotizacionService;


    @GetMapping
    public ResponseEntity<Page<CotizacionRespuestaDTO>> listarCotizaciones(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {


        Pageable pageable = PageRequest.of(pagina, tamano);
        return ResponseEntity.ok(cotizacionService.listarTodas(pageable));
    }


    @GetMapping("/pendientes")
    public ResponseEntity<Page<CotizacionRespuestaDTO>> listarPendientes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {


        Pageable pageable = PageRequest.of(pagina, tamano);
        return ResponseEntity.ok(cotizacionService.listarPendientesOEnProceso(pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<CotizacionRespuestaDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(cotizacionService.buscarPorId(id));
    }


    @PostMapping
    public ResponseEntity<CotizacionRespuestaDTO> registrarCotizacion(@Valid @RequestBody CotizacionRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacionService.registrarCotizacion(datos));
    }


    @PutMapping("/{id}")
    public ResponseEntity<CotizacionRespuestaDTO> actualizarCotizacion(
            @PathVariable Integer id,
            @Valid @RequestBody CotizacionRequestDTO datos) {


        return ResponseEntity.ok(cotizacionService.actualizarCotizacion(id, datos));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCotizacion(@PathVariable Integer id) {
        cotizacionService.eliminarCotizacion(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/aprobar")
    public ResponseEntity<CotizacionRespuestaDTO> aprobarCotizacion(@PathVariable Integer id) {
        return ResponseEntity.ok(cotizacionService.aprobarCotizacion(id));
    }


    @PostMapping("/{id}/rechazar")
    public ResponseEntity<CotizacionRespuestaDTO> rechazarCotizacion(@PathVariable Integer id) {
        return ResponseEntity.ok(cotizacionService.rechazarCotizacion(id));
    }
}
