/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Curso;
import sv.edu.udb.model.EstadoInscripcion;
import sv.edu.udb.model.Inscripcion;
import sv.edu.udb.model.Participante;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.CursoRepository;
import sv.edu.udb.repository.EstadoInscripcionRepository;
import sv.edu.udb.repository.InscripcionRepository;
import sv.edu.udb.repository.ParticipanteRepository;
/**
 *
 * @author crist
 */
@Service
@RequiredArgsConstructor
public class InscripcionService {
    
     private final InscripcionRepository inscripcionRepository;
    private final EstadoInscripcionRepository estadoInscripcionRepository;
    private final ClienteRepository clienteRepository;
    private final CursoRepository cursoRepository;
    private final ParticipanteRepository participanteRepository;

    @Transactional(readOnly = true)
    public Page<Inscripcion> listarInscripciones(
            String busqueda,
            Pageable paginacion) {

        if (busqueda == null || busqueda.isBlank()) {
            return inscripcionRepository.findAll(paginacion);
        }

        return inscripcionRepository
                .findByClienteNombreContainingIgnoreCaseOrCursoNombreContainingIgnoreCase(
                        busqueda,
                        busqueda,
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public List<Inscripcion> listarPorCliente(
            Integer idCliente) {

        return inscripcionRepository
                .findByCliente_IdClienteOrderByFechaDesc(
                        idCliente
                );
    }

    @Transactional(readOnly = true)
    public Inscripcion buscarPorId(Integer id) {

        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró la inscripción con ID: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {

        return clienteRepository.findAll(
                Sort.by("nombre").ascending()
        );
    }

    @Transactional(readOnly = true)
    public List<Curso> listarCursos() {

        return cursoRepository.findAll(
                Sort.by("nombre").ascending()
        );
    }

    @Transactional(readOnly = true)
    public List<Participante> listarParticipantes() {

        return participanteRepository.findAll(
                Sort.by("apellido").ascending()
                        .and(Sort.by("nombre").ascending())
        );
    }

    @Transactional(readOnly = true)
    public List<EstadoInscripcion> listarEstados() {

        return estadoInscripcionRepository.findAll(
                Sort.by("idEstadoInscripcion").ascending()
        );
    }

    @Transactional
    public Inscripcion registrarInscripcion(
            Inscripcion inscripcion,
            List<Integer> idsParticipantes) {

        asignarRelaciones(
                inscripcion,
                idsParticipantes
        );

        return inscripcionRepository.save(inscripcion);
    }

    @Transactional
    public Inscripcion registrarInscripcion(
            Inscripcion inscripcion) {

        List<Integer> idsParticipantes
                = obtenerIdsParticipantes(
                        inscripcion.getParticipantes()
                );

        return registrarInscripcion(
                inscripcion,
                idsParticipantes
        );
    }

    @Transactional
    public Inscripcion actualizarInscripcion(
            Integer id,
            Inscripcion datosInscripcion,
            List<Integer> idsParticipantes) {

        Inscripcion inscripcion = buscarPorId(id);

        inscripcion.setFecha(
                datosInscripcion.getFecha()
        );

        inscripcion.setCliente(
                datosInscripcion.getCliente()
        );

        inscripcion.setCurso(
                datosInscripcion.getCurso()
        );

        inscripcion.setEstadoInscripcion(
                datosInscripcion.getEstadoInscripcion()
        );

        asignarRelaciones(
                inscripcion,
                idsParticipantes
        );

        return inscripcionRepository.save(inscripcion);
    }

    @Transactional
    public Inscripcion actualizarInscripcion(
            Integer id,
            Inscripcion datosInscripcion) {

        List<Integer> idsParticipantes
                = obtenerIdsParticipantes(
                        datosInscripcion.getParticipantes()
                );

        return actualizarInscripcion(
                id,
                datosInscripcion,
                idsParticipantes
        );
    }

    @Transactional
    public void eliminarInscripcion(Integer id) {

        Inscripcion inscripcion = buscarPorId(id);

        try {

            inscripcionRepository.delete(inscripcion);
            inscripcionRepository.flush();

        } catch (DataIntegrityViolationException excepcion) {

            throw new IllegalArgumentException(
                    "No se puede eliminar la inscripción "
                    + "porque tiene pagos u otros registros asociados."
            );
        }
    }

    private void asignarRelaciones(
            Inscripcion inscripcion,
            List<Integer> idsParticipantes) {

        if (inscripcion.getCliente() == null
                || inscripcion.getCliente()
                        .getIdCliente() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un cliente"
            );
        }

        if (inscripcion.getCurso() == null
                || inscripcion.getCurso()
                        .getIdCurso() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un curso"
            );
        }

        if (inscripcion.getEstadoInscripcion() == null
                || inscripcion.getEstadoInscripcion()
                        .getIdEstadoInscripcion() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un estado"
            );
        }

        Cliente cliente = clienteRepository.findById(
                inscripcion.getCliente().getIdCliente()
        ).orElseThrow(() -> new IllegalArgumentException(
                "El cliente seleccionado no existe"
        ));

        Curso curso = cursoRepository.findById(
                inscripcion.getCurso().getIdCurso()
        ).orElseThrow(() -> new IllegalArgumentException(
                "El curso seleccionado no existe"
        ));

        EstadoInscripcion estado
                = estadoInscripcionRepository.findById(
                        inscripcion
                                .getEstadoInscripcion()
                                .getIdEstadoInscripcion()
                ).orElseThrow(() -> new IllegalArgumentException(
                    "El estado seleccionado no existe"
                ));

        if (idsParticipantes == null
                || idsParticipantes.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un participante"
            );
        }

        Set<Integer> idsSinRepetir
                = new LinkedHashSet<>(
                        idsParticipantes
                );

        List<Participante> participantes
                = participanteRepository.findAllById(
                        idsSinRepetir
                );

        if (participantes.size()
                != idsSinRepetir.size()) {

            throw new IllegalArgumentException(
                    "Uno o más participantes seleccionados no existen"
            );
        }

        inscripcion.setCliente(cliente);
        inscripcion.setCurso(curso);
        inscripcion.setEstadoInscripcion(estado);

        inscripcion.setParticipantes(
                new LinkedHashSet<>(participantes)
        );
    }

    private List<Integer> obtenerIdsParticipantes(
            Set<Participante> participantes) {

        if (participantes == null) {
            return List.of();
        }

        return participantes.stream()
                .filter(participante
                        -> participante != null
                        && participante
                                .getIdParticipante() != null
                )
                .map(Participante::getIdParticipante)
                .collect(Collectors.toList());
    }
    
}
