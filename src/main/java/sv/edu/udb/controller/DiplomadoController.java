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
import sv.edu.udb.model.Diplomado;
import sv.edu.udb.service.DiplomadoService;

@RestController
@RequestMapping("/api/diplomados")
@RequiredArgsConstructor
public class DiplomadoController {

    private final DiplomadoService diplomadoService;

    @GetMapping
    public ResponseEntity<Page<Diplomado>> listarDiplomados(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {

        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();

        Pageable pageable = PageRequest.of(pagina, tamano, sort);
        return ResponseEntity.ok(diplomadoService.listarDiplomados(buscar, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Diplomado> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(diplomadoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Diplomado> registrarDiplomado(@Valid @RequestBody Diplomado diplomado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diplomadoService.registrarDiplomado(diplomado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Diplomado> actualizarDiplomado(
            @PathVariable Integer id, @Valid @RequestBody Diplomado diplomado) {
        return ResponseEntity.ok(diplomadoService.actualizarDiplomado(id, diplomado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDiplomado(@PathVariable Integer id) {
        diplomadoService.eliminarDiplomado(id);
        return ResponseEntity.noContent().build();
    }
}