package sv.edu.udb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.service.CateringService;

@RestController
@RequestMapping("/api/catering")
@RequiredArgsConstructor
public class CateringController {

    private final CateringService cateringService;

    @GetMapping
    public ResponseEntity<Page<ServicioCatering>> listarServicios(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {

        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();

        Pageable pageable = PageRequest.of(pagina, tamano, sort);
        return ResponseEntity.ok(cateringService.listarServicios(buscar, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioCatering> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(cateringService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ServicioCatering> registrarServicio(@Valid @RequestBody ServicioCatering servicio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cateringService.registrarServicio(servicio));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioCatering> actualizarServicio(
            @PathVariable Integer id, @Valid @RequestBody ServicioCatering servicio) {
        return ResponseEntity.ok(cateringService.actualizarServicio(id, servicio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Integer id) {
        cateringService.eliminarServicio(id);
        return ResponseEntity.noContent().build();
    }
}