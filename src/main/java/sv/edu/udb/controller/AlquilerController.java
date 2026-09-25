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
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.service.AlquilerService;
/**
 *
 * @author crist
 */
@RestController
@RequestMapping("/api/alquileres")
@RequiredArgsConstructor
public class AlquilerController {
    
    private final AlquilerService alquilerService;

    @GetMapping
    public ResponseEntity<Page<Alquiler>> listarAlquileres(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "horaInicio") String ordenarPor,
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
                alquilerService.listarAlquileres(
                        buscar,
                        paginacion
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alquiler> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                alquilerService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<Alquiler> registrarAlquiler(
            @Valid @RequestBody Alquiler alquiler) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        alquilerService.registrarAlquiler(
                                alquiler
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alquiler> actualizarAlquiler(
            @PathVariable Integer id,
            @Valid @RequestBody Alquiler alquiler) {

        return ResponseEntity.ok(
                alquilerService.actualizarAlquiler(
                        id,
                        alquiler
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAlquiler(
            @PathVariable Integer id) {

        alquilerService.eliminarAlquiler(id);

        return ResponseEntity.noContent().build();
    }    
}
