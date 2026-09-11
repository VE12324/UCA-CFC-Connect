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
import sv.edu.udb.model.Espacio;
import sv.edu.udb.service.EspacioService;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
public class EspacioController {

    private final EspacioService espacioService;

    @GetMapping
    public ResponseEntity<Page<Espacio>> listarEspacios(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {

        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();

        Pageable pageable = PageRequest.of(pagina, tamano, sort);
        return ResponseEntity.ok(espacioService.listarEspacios(buscar, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Espacio> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(espacioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Espacio> registrarEspacio(@Valid @RequestBody Espacio espacio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(espacioService.registrarEspacio(espacio));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Espacio> actualizarEspacio(
            @PathVariable Integer id, @Valid @RequestBody Espacio espacio) {
        return ResponseEntity.ok(espacioService.actualizarEspacio(id, espacio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEspacio(@PathVariable Integer id) {
        espacioService.eliminarEspacio(id);
        return ResponseEntity.noContent().build();
    }
}
