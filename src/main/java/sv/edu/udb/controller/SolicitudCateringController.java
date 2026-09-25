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
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.service.SolicitudCateringService;
/**
 *
 * @author crist
 */
@RestController
@RequestMapping("/api/solicitudes-catering")
@RequiredArgsConstructor
public class SolicitudCateringController {
    
    private final SolicitudCateringService solicitudCateringService;

    @GetMapping
    public ResponseEntity<Page<SolicitudCatering>> listarSolicitudes(
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
                solicitudCateringService.listarSolicitudes(
                        buscar,
                        paginacion
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudCatering> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                solicitudCateringService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<SolicitudCatering> registrarSolicitud(
            @Valid @RequestBody SolicitudCatering solicitud) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        solicitudCateringService.registrarSolicitud(
                                solicitud
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitudCatering> actualizarSolicitud(
            @PathVariable Integer id,
            @Valid @RequestBody SolicitudCatering solicitud) {

        return ResponseEntity.ok(
                solicitudCateringService.actualizarSolicitud(
                        id,
                        solicitud
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSolicitud(
            @PathVariable Integer id) {

        solicitudCateringService.eliminarSolicitud(id);

        return ResponseEntity.noContent().build();
    }
    
}
