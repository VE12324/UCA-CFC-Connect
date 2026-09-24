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
import sv.edu.udb.model.Modalidad;
import sv.edu.udb.service.ModalidadService;


@RestController
@RequestMapping("/api/modalidades")
@RequiredArgsConstructor
public class ModalidadController {


    private final ModalidadService modalidadService;


    @GetMapping
    public ResponseEntity<Page<Modalidad>> listarModalidades(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {


        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();


        Pageable pageable = PageRequest.of(pagina, tamano, sort);


        return ResponseEntity.ok(modalidadService.listarModalidades(buscar, pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Modalidad> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(modalidadService.buscarPorId(id));
    }


    @PostMapping
    public ResponseEntity<Modalidad> registrarModalidad(@Valid @RequestBody Modalidad modalidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modalidadService.registrarModalidad(modalidad));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Modalidad> actualizarModalidad(@PathVariable Integer id, @Valid @RequestBody Modalidad modalidad) {
        return ResponseEntity.ok(modalidadService.actualizarModalidad(id, modalidad));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarModalidad(@PathVariable Integer id) {
        modalidadService.eliminarModalidad(id);
        return ResponseEntity.noContent().build();
    }
}

