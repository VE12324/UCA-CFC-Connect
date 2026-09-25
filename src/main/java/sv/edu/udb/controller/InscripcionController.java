/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.model.Inscripcion;
import sv.edu.udb.service.InscripcionService;
/**
 *
 * @author crist
 */
@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {
    
     private final InscripcionService inscripcionService;

    @GetMapping
    public ResponseEntity<Page<Inscripcion>> listarInscripciones(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direccion) {

        Sort orden = direccion.equalsIgnoreCase("asc")
                ? Sort.by(ordenarPor).ascending()
                : Sort.by(ordenarPor).descending();

        Pageable paginacion = PageRequest.of(
                pagina,
                tamano,
                orden
        );

        return ResponseEntity.ok(
                inscripcionService.listarInscripciones(
                        buscar,
                        paginacion
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inscripcion> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                inscripcionService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<Inscripcion> registrarInscripcion(
            @Valid @RequestBody Inscripcion inscripcion) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        inscripcionService.registrarInscripcion(
                                inscripcion
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inscripcion> actualizarInscripcion(
            @PathVariable Integer id,
            @Valid @RequestBody Inscripcion inscripcion) {

        return ResponseEntity.ok(
                inscripcionService.actualizarInscripcion(
                        id,
                        inscripcion
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInscripcion(
            @PathVariable Integer id) {

        inscripcionService.eliminarInscripcion(id);

        return ResponseEntity.noContent().build();
    }
    
}
