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
import sv.edu.udb.model.Actividad;
import sv.edu.udb.service.ActividadService;
/**
 *
 * @author crist
 */
@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class ActividadController {
    
     private final ActividadService actividadService;

    @GetMapping
    public ResponseEntity<Page<Actividad>> listarActividades(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {

        Sort orden = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();

        Pageable paginacion = PageRequest.of(
                pagina,
                tamano,
                orden
        );

        return ResponseEntity.ok(
                actividadService.listarActividades(
                        buscar,
                        paginacion
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Actividad> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                actividadService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<Actividad> registrarActividad(
            @Valid @RequestBody Actividad actividad) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        actividadService.registrarActividad(
                                actividad
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Actividad> actualizarActividad(
            @PathVariable Integer id,
            @Valid @RequestBody Actividad actividad) {

        return ResponseEntity.ok(
                actividadService.actualizarActividad(
                        id,
                        actividad
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarActividad(
            @PathVariable Integer id) {

        actividadService.eliminarActividad(id);

        return ResponseEntity.noContent().build();
    }
    
}
