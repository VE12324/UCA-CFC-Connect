package sv.edu.udb.controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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
import sv.edu.udb.model.Participante;
import sv.edu.udb.service.ParticipanteService;
/**
 *
 * @author crist
 */
@RestController
@RequestMapping("/api/participantes")
@RequiredArgsConstructor
public class ParticipanteController {
    
    private final ParticipanteService participanteServicio;

    @GetMapping
    public ResponseEntity<Page<Participante>> listarParticipantes(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {

        Sort orden;

        if (direccion.equalsIgnoreCase("desc")) {
            orden = Sort.by(ordenarPor).descending();
        } else {
            orden = Sort.by(ordenarPor).ascending();
        }

        Pageable paginacion = PageRequest.of(
                pagina,
                tamano,
                orden
        );

        Page<Participante> participantes =
                participanteServicio.listarParticipantes(
                        buscar,
                        paginacion
                );

        return ResponseEntity.ok(participantes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Participante> buscarParticipantePorId(
            @PathVariable Integer id) {

        Participante participante =
                participanteServicio.buscarPorId(id);

        return ResponseEntity.ok(participante);
    }

    @PostMapping
    public ResponseEntity<Participante> registrarParticipante(
            @Valid @RequestBody Participante participante) {

        Participante participanteRegistrado =
                participanteServicio.registrarParticipante(
                        participante
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(participanteRegistrado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Participante> actualizarParticipante(
            @PathVariable Integer id,
            @Valid @RequestBody Participante participante) {

        Participante participanteActualizado =
                participanteServicio.actualizarParticipante(
                        id,
                        participante
                );

        return ResponseEntity.ok(participanteActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarParticipante(
            @PathVariable Integer id) {

        participanteServicio.eliminarParticipante(id);

        return ResponseEntity.noContent().build();
    }
    
}
