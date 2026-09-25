/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Actividad;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Curso;
import sv.edu.udb.model.Diplomado;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.repository.ActividadRepository;
import sv.edu.udb.repository.AlquilerRepository;
import sv.edu.udb.repository.CursoRepository;
import sv.edu.udb.repository.DiplomadoRepository;
import sv.edu.udb.repository.SolicitudCateringRepository;
/**
 *
 * @author crist
 */
@Service
@RequiredArgsConstructor
public class ActividadService {
    
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "INSTITUCIONAL",
            "CURSO",
            "DIPLOMADO",
            "ALQUILER",
            "CATERING"
    );

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm");

    private final ActividadRepository actividadRepository;
    private final CursoRepository cursoRepository;
    private final DiplomadoRepository diplomadoRepository;
    private final AlquilerRepository alquilerRepository;
    private final SolicitudCateringRepository solicitudCateringRepository;

    @Transactional(readOnly = true)
    public Page<Actividad> listarActividades(
            String busqueda,
            Pageable paginacion) {

        if (busqueda == null || busqueda.isBlank()) {
            return actividadRepository.findAll(paginacion);
        }

        return actividadRepository
                .findByNombreContainingIgnoreCaseOrTipoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                        busqueda,
                        busqueda,
                        busqueda,
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public Actividad buscarPorId(Integer id) {

        return actividadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró la actividad con ID: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<Curso> listarCursos() {

        return cursoRepository.findAll(
                Sort.by("nombre").ascending()
        );
    }

    @Transactional(readOnly = true)
    public List<Diplomado> listarDiplomados() {

        return diplomadoRepository.findAll(
                Sort.by("nombre").ascending()
        );
    }

    @Transactional(readOnly = true)
    public List<Alquiler> listarAlquileres() {

        return alquilerRepository.findAll(
                Sort.by("horaInicio").descending()
        );
    }

    @Transactional(readOnly = true)
    public List<SolicitudCatering> listarSolicitudesCatering() {

        return solicitudCateringRepository.findAll(
                Sort.by("fecha").descending()
                        .and(Sort.by("hora").descending())
        );
    }

    @Transactional
    public Actividad registrarActividad(
            Actividad actividad) {

        prepararActividad(actividad);

        return actividadRepository.save(actividad);
    }

    @Transactional
    public Actividad actualizarActividad(
            Integer id,
            Actividad datosActividad) {

        Actividad actividad = buscarPorId(id);

        actividad.setNombre(
                datosActividad.getNombre()
        );

        actividad.setTipo(
                datosActividad.getTipo()
        );

        actividad.setFecha(
                datosActividad.getFecha()
        );

        actividad.setHorario(
                datosActividad.getHorario()
        );

        actividad.setHoraInicio(
                datosActividad.getHoraInicio()
        );

        actividad.setHoraFin(
                datosActividad.getHoraFin()
        );

        actividad.setDescripcion(
                datosActividad.getDescripcion()
        );

        actividad.setCurso(
                datosActividad.getCurso()
        );

        actividad.setDiplomado(
                datosActividad.getDiplomado()
        );

        actividad.setAlquiler(
                datosActividad.getAlquiler()
        );

        actividad.setSolicitudCatering(
                datosActividad.getSolicitudCatering()
        );

        prepararActividad(actividad);

        return actividadRepository.save(actividad);
    }

    @Transactional
    public void eliminarActividad(Integer id) {

        Actividad actividad = buscarPorId(id);

        try {
            actividadRepository.delete(actividad);
            actividadRepository.flush();

        } catch (DataIntegrityViolationException excepcion) {

            throw new IllegalArgumentException(
                    "No se puede eliminar la actividad porque tiene otros registros asociados."
            );
        }
    }

    private void prepararActividad(
            Actividad actividad) {

        validarDatosGenerales(actividad);
        asignarRelacionSegunTipo(actividad);
        prepararHorario(actividad);

        actividad.setNombre(
                actividad.getNombre().trim()
        );

        if (actividad.getDescripcion() != null) {

            actividad.setDescripcion(
                    actividad.getDescripcion().trim()
            );
        }

        if (actividad.getHorario() != null) {

            actividad.setHorario(
                    actividad.getHorario().trim()
            );
        }
    }

    private void validarDatosGenerales(
            Actividad actividad) {

        if (actividad.getNombre() == null
                || actividad.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre de la actividad es obligatorio"
            );
        }

        if (actividad.getTipo() == null
                || actividad.getTipo().isBlank()) {

            actividad.setTipo("INSTITUCIONAL");
        }

        String tipoNormalizado
                = actividad.getTipo()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (!TIPOS_PERMITIDOS.contains(
                tipoNormalizado
        )) {

            throw new IllegalArgumentException(
                    "El tipo de actividad no es válido"
            );
        }

        actividad.setTipo(tipoNormalizado);

        if (actividad.getFecha() == null) {

            throw new IllegalArgumentException(
                    "La fecha de la actividad es obligatoria"
            );
        }
    }

    private void prepararHorario(
            Actividad actividad) {

        boolean tieneInicio
                = actividad.getHoraInicio() != null;

        boolean tieneFin
                = actividad.getHoraFin() != null;

        if (tieneInicio != tieneFin) {

            throw new IllegalArgumentException(
                    "Debe ingresar tanto la hora de inicio como la hora de finalización"
            );
        }

        if (tieneInicio) {

            if (!actividad.getHoraFin()
                    .isAfter(actividad.getHoraInicio())) {

                throw new IllegalArgumentException(
                        "La hora de finalización debe ser posterior a la hora de inicio"
                );
            }

            if (!actividad.getFecha().equals(
                    actividad.getHoraInicio().toLocalDate()
            )) {

                throw new IllegalArgumentException(
                        "La fecha de la actividad debe coincidir con la fecha de inicio"
                );
            }

            if (actividad.getHorario() == null
                    || actividad.getHorario().isBlank()) {

                String horario
                        = actividad.getHoraInicio()
                                .format(FORMATO_HORA)
                        + " - "
                        + actividad.getHoraFin()
                                .format(FORMATO_HORA);

                actividad.setHorario(horario);
            }
        }
    }

    private void asignarRelacionSegunTipo(
            Actividad actividad) {

        String tipo = actividad.getTipo();

        switch (tipo) {

            case "INSTITUCIONAL" -> {
                limpiarRelaciones(actividad);
            }

            case "CURSO" -> {
                Integer idCurso = obtenerIdCurso(
                        actividad
                );

                Curso curso = cursoRepository.findById(
                        idCurso
                ).orElseThrow(() -> new IllegalArgumentException(
                        "El curso seleccionado no existe"
                ));

                limpiarRelaciones(actividad);
                actividad.setCurso(curso);
            }

            case "DIPLOMADO" -> {
                Integer idDiplomado = obtenerIdDiplomado(
                        actividad
                );

                Diplomado diplomado
                        = diplomadoRepository.findById(
                                idDiplomado
                        ).orElseThrow(() -> new IllegalArgumentException(
                            "El diplomado seleccionado no existe"
                        ));

                limpiarRelaciones(actividad);
                actividad.setDiplomado(diplomado);
            }

            case "ALQUILER" -> {
                Integer idAlquiler = obtenerIdAlquiler(
                        actividad
                );

                Alquiler alquiler
                        = alquilerRepository.findById(
                                idAlquiler
                        ).orElseThrow(() -> new IllegalArgumentException(
                            "El alquiler seleccionado no existe"
                        ));

                limpiarRelaciones(actividad);
                actividad.setAlquiler(alquiler);
            }

            case "CATERING" -> {
                Integer idSolicitud
                        = obtenerIdSolicitudCatering(
                                actividad
                        );

                SolicitudCatering solicitud
                        = solicitudCateringRepository.findById(
                                idSolicitud
                        ).orElseThrow(() -> new IllegalArgumentException(
                            "La solicitud de catering seleccionada no existe"
                        ));

                limpiarRelaciones(actividad);
                actividad.setSolicitudCatering(solicitud);
            }

            default -> throw new IllegalArgumentException(
                    "El tipo de actividad no es válido"
            );
        }
    }

    private Integer obtenerIdCurso(
            Actividad actividad) {

        if (actividad.getCurso() == null
                || actividad.getCurso()
                        .getIdCurso() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un curso"
            );
        }

        return actividad.getCurso().getIdCurso();
    }

    private Integer obtenerIdDiplomado(
            Actividad actividad) {

        if (actividad.getDiplomado() == null
                || actividad.getDiplomado()
                        .getIdDiplomado() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un diplomado"
            );
        }

        return actividad.getDiplomado()
                .getIdDiplomado();
    }

    private Integer obtenerIdAlquiler(
            Actividad actividad) {

        if (actividad.getAlquiler() == null
                || actividad.getAlquiler()
                        .getIdAlquiler() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un alquiler"
            );
        }

        return actividad.getAlquiler()
                .getIdAlquiler();
    }

    private Integer obtenerIdSolicitudCatering(
            Actividad actividad) {

        if (actividad.getSolicitudCatering() == null
                || actividad.getSolicitudCatering()
                        .getIdSolicitud() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una solicitud de catering"
            );
        }

        return actividad.getSolicitudCatering()
                .getIdSolicitud();
    }

    private void limpiarRelaciones(
            Actividad actividad) {

        actividad.setCurso(null);
        actividad.setDiplomado(null);
        actividad.setAlquiler(null);
        actividad.setSolicitudCatering(null);
    }
    
}
