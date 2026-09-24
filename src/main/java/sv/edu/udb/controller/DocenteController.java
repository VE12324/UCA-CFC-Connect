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
import sv.edu.udb.model.Docente;
import sv.edu.udb.service.DocenteService;


@RestController
@RequestMapping("/api/docentes")
@RequiredArgsConstructor
public class DocenteController {


    private final DocenteService docenteService;


    @GetMapping
    public ResponseEntity<Page<Docente>> listarDocentes(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {


        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();


        Pageable pageable = PageRequest.of(pagina, tamano, sort);


        return ResponseEntity.ok(docenteService.listarDocentes(buscar, pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Docente> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(docenteService.buscarPorId(id));
    }


    @PostMapping
    public ResponseEntity<Docente> registrarDocente(@Valid @RequestBody Docente docente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteService.registrarDocente(docente));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Docente> actualizarDocente(@PathVariable Integer id, @Valid @RequestBody Docente docente) {
        return ResponseEntity.ok(docenteService.actualizarDocente(id, docente));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDocente(@PathVariable Integer id) {
        docenteService.eliminarDocente(id);
        return ResponseEntity.noContent().build();
    }
}
