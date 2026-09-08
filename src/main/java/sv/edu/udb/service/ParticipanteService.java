/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Participante;
import sv.edu.udb.repository.ParticipanteRepository;
/**
 *
 * @author crist
 */
@Service
@RequiredArgsConstructor
public class ParticipanteService {
        
     private final ParticipanteRepository participanteRepositorio;

    @Transactional(readOnly = true)
    public Page<Participante> listarParticipantes(
            String busqueda,
            Pageable paginacion) {

        if (busqueda == null || busqueda.isBlank()) {
            return participanteRepositorio.findAll(paginacion);
        }

        return participanteRepositorio
                .findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrDuiContainingIgnoreCase(
                        busqueda,
                        busqueda,
                        busqueda,
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public Participante buscarPorId(Integer id) {

        return participanteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró el participante con ID: " + id
                ));
    }

    @Transactional
    public Participante registrarParticipante(Participante participante) {

        validarDuiDuplicado(participante.getDui(), null);

        return participanteRepositorio.save(participante);
    }

    @Transactional
    public Participante actualizarParticipante(
            Integer id,
            Participante datosParticipante) {

        Participante participante = buscarPorId(id);

        validarDuiDuplicado(datosParticipante.getDui(), id);

        participante.setDui(datosParticipante.getDui());
        participante.setNombre(datosParticipante.getNombre());
        participante.setApellido(datosParticipante.getApellido());
        participante.setCorreo(datosParticipante.getCorreo());
        participante.setTelefono(datosParticipante.getTelefono());

        return participanteRepositorio.save(participante);
    }

    @Transactional
    public void eliminarParticipante(Integer id) {

        Participante participante = buscarPorId(id);

        participanteRepositorio.delete(participante);
    }

    private void validarDuiDuplicado(
            String dui,
            Integer idParticipanteActual) {

        if (dui == null || dui.isBlank()) {
            return;
        }

        participanteRepositorio.findByDui(dui)
                .filter(participante ->
                        idParticipanteActual == null
                        || !participante.getIdParticipante()
                                .equals(idParticipanteActual)
                )
                .ifPresent(participante -> {
                    throw new IllegalArgumentException(
                            "Ya existe un participante registrado con el DUI: "
                            + dui
                    );
                });
    }
}
