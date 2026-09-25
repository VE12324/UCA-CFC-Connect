/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.model.Actividad;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Inscripcion;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.repository.ActividadRepository;
import sv.edu.udb.repository.AlquilerRepository;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.InscripcionRepository;
import sv.edu.udb.repository.SolicitudCateringRepository;
/**
 *
 * @author crist
 */
@Service
@RequiredArgsConstructor
public class PortalClienteService {
     private final ClienteRepository
            clienteRepository;

    private final InscripcionRepository
            inscripcionRepository;

    private final AlquilerRepository
            alquilerRepository;

    private final SolicitudCateringRepository
            solicitudCateringRepository;

    private final ActividadRepository
            actividadRepository;

    @Transactional(readOnly = true)
    public Page<Inscripcion> listarInscripciones(
            String correo,
            Pageable paginacion) {

        validarCorreo(
                correo
        );

        return inscripcionRepository
                .findByCliente_Usuario_EmailIgnoreCase(
                        correo.trim(),
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public Page<Alquiler> listarAlquileres(
            String correo,
            Pageable paginacion) {

        validarCorreo(
                correo
        );

        return alquilerRepository
                .findByCliente_Usuario_EmailIgnoreCase(
                        correo.trim(),
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public Page<SolicitudCatering> listarCatering(
            String correo,
            Pageable paginacion) {

        validarCorreo(
                correo
        );

        return solicitudCateringRepository
                .findByCliente_Usuario_EmailIgnoreCase(
                        correo.trim(),
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public List<Actividad> listarAgenda(
            String correo) {

        validarCorreo(
                correo
        );

        Cliente cliente = clienteRepository
                .findByUsuario_EmailIgnoreCase(
                        correo.trim()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el cliente asociado con la cuenta."
                        )
                );

        Integer idCliente =
                cliente.getIdCliente();

        List<Inscripcion> inscripciones =
                inscripcionRepository
                        .findByCliente_IdClienteOrderByFechaDesc(
                                idCliente
                        );

        Set<Integer> idsCursos =
                new HashSet<>();

        for (Inscripcion inscripcion : inscripciones) {

            if (inscripcion.getCurso() != null
                    && inscripcion.getCurso()
                            .getIdCurso() != null) {

                idsCursos.add(
                        inscripcion.getCurso()
                                .getIdCurso()
                );
            }
        }

        List<Actividad> actividades =
                actividadRepository.findAll(
                        Sort.by("fecha")
                                .descending()
                                .and(
                                        Sort.by("horaInicio")
                                                .ascending()
                                )
                );

        return actividades
                .stream()
                .filter(actividad ->
                        perteneceAlCliente(
                                actividad,
                                idCliente,
                                idsCursos
                        )
                )
                .collect(
                        Collectors.toList()
                );
    }

    private boolean perteneceAlCliente(
            Actividad actividad,
            Integer idCliente,
            Set<Integer> idsCursos) {

        if (esActividadGeneral(
                actividad
        )) {

            return true;
        }

        if (actividad.getCurso() != null
                && actividad.getCurso()
                        .getIdCurso() != null
                && idsCursos.contains(
                        actividad.getCurso()
                                .getIdCurso()
                )) {

            return true;
        }

        if (actividad.getAlquiler() != null
                && actividad.getAlquiler()
                        .getCliente() != null
                && idCliente.equals(
                        actividad.getAlquiler()
                                .getCliente()
                                .getIdCliente()
                )) {

            return true;
        }

        return actividad.getSolicitudCatering() != null
                && actividad.getSolicitudCatering()
                        .getCliente() != null
                && idCliente.equals(
                        actividad.getSolicitudCatering()
                                .getCliente()
                                .getIdCliente()
                );
    }

    private boolean esActividadGeneral(
            Actividad actividad) {

        return actividad.getCurso() == null
                && actividad.getDiplomado() == null
                && actividad.getAlquiler() == null
                && actividad.getSolicitudCatering() == null;
    }

    private void validarCorreo(
            String correo) {

        if (correo == null
                || correo.isBlank()) {

            throw new IllegalArgumentException(
                    "No se pudo identificar la cuenta del cliente."
            );
        }
    }
    
}
