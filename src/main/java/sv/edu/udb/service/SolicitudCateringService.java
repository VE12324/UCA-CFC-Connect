/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.service;

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
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.model.SolicitudCatering;
import sv.edu.udb.repository.CateringRepository;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.SolicitudCateringRepository;
/**
 *
 * @author crist
 */
@Service
@RequiredArgsConstructor
public class SolicitudCateringService {
    
     private static final Set<String> ESTADOS_PERMITIDOS = Set.of(
            "PENDIENTE",
            "CONFIRMADA",
            "CANCELADA",
            "FINALIZADA"
    );

    private final SolicitudCateringRepository solicitudCateringRepository;
    private final ClienteRepository clienteRepository;
    private final CateringRepository cateringRepository;

    @Transactional(readOnly = true)
    public Page<SolicitudCatering> listarSolicitudes(
            String busqueda,
            Pageable paginacion) {

        if (busqueda == null || busqueda.isBlank()) {
            return solicitudCateringRepository.findAll(
                    paginacion
            );
        }

        return solicitudCateringRepository
                .findByClienteNombreContainingIgnoreCaseOrServicioCateringNombreContainingIgnoreCaseOrLugarContainingIgnoreCase(
                        busqueda,
                        busqueda,
                        busqueda,
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public SolicitudCatering buscarPorId(Integer id) {

        return solicitudCateringRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró la solicitud de catering con ID: "
                        + id
                ));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {

        return clienteRepository.findAll(
                Sort.by("nombre").ascending()
        );
    }

    @Transactional(readOnly = true)
    public List<ServicioCatering> listarServicios() {

        return cateringRepository.findAll(
                Sort.by("nombre").ascending()
        );
    }

    @Transactional
    public SolicitudCatering registrarSolicitud(
            SolicitudCatering solicitud) {

        asignarClienteYServicio(solicitud);
        validarSolicitud(solicitud);

        return solicitudCateringRepository.save(
                solicitud
        );
    }

    @Transactional
    public SolicitudCatering actualizarSolicitud(
            Integer id,
            SolicitudCatering datosSolicitud) {

        SolicitudCatering solicitud = buscarPorId(id);

        solicitud.setCantidadAsistentes(
                datosSolicitud.getCantidadAsistentes()
        );

        solicitud.setMenu(
                datosSolicitud.getMenu()
        );

        solicitud.setFecha(
                datosSolicitud.getFecha()
        );

        solicitud.setHora(
                datosSolicitud.getHora()
        );

        solicitud.setLugar(
                datosSolicitud.getLugar()
        );

        solicitud.setEstado(
                datosSolicitud.getEstado()
        );

        solicitud.setCliente(
                datosSolicitud.getCliente()
        );

        solicitud.setServicioCatering(
                datosSolicitud.getServicioCatering()
        );

        asignarClienteYServicio(solicitud);
        validarSolicitud(solicitud);

        return solicitudCateringRepository.save(
                solicitud
        );
    }

    @Transactional
    public void eliminarSolicitud(Integer id) {

        SolicitudCatering solicitud = buscarPorId(id);

        try {
            solicitudCateringRepository.delete(
                    solicitud
            );

            solicitudCateringRepository.flush();

        } catch (DataIntegrityViolationException excepcion) {

            throw new IllegalArgumentException(
                    "No se puede eliminar la solicitud porque tiene pagos, actividades u otros registros asociados."
            );
        }
    }

    private void asignarClienteYServicio(
            SolicitudCatering solicitud) {

        if (solicitud.getCliente() == null
                || solicitud.getCliente()
                        .getIdCliente() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un cliente"
            );
        }

        if (solicitud.getServicioCatering() == null
                || solicitud.getServicioCatering()
                        .getIdCatering() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un servicio de catering"
            );
        }

        Cliente cliente = clienteRepository.findById(
                solicitud.getCliente().getIdCliente()
        ).orElseThrow(() -> new IllegalArgumentException(
                "El cliente seleccionado no existe"
        ));

        ServicioCatering servicio
                = cateringRepository.findById(
                        solicitud.getServicioCatering()
                                .getIdCatering()
                ).orElseThrow(() -> new IllegalArgumentException(
                    "El servicio de catering seleccionado no existe"
                ));

        solicitud.setCliente(cliente);
        solicitud.setServicioCatering(servicio);
    }

    private void validarSolicitud(
            SolicitudCatering solicitud) {

        ServicioCatering servicio
                = solicitud.getServicioCatering();

        if (solicitud.getCantidadAsistentes() == null
                || solicitud.getCantidadAsistentes() < 1) {

            throw new IllegalArgumentException(
                    "La cantidad de asistentes debe ser al menos 1"
            );
        }

        if (solicitud.getCantidadAsistentes()
                < servicio.getCapacidadMinima()) {

            throw new IllegalArgumentException(
                    "La cantidad de asistentes es menor que la capacidad mínima del servicio seleccionado"
            );
        }

        if (solicitud.getCantidadAsistentes()
                > servicio.getCapacidadMaxima()) {

            throw new IllegalArgumentException(
                    "La cantidad de asistentes supera la capacidad máxima del servicio seleccionado"
            );
        }

        if (!"DISPONIBLE".equalsIgnoreCase(
                servicio.getEstado()
        )) {

            throw new IllegalArgumentException(
                    "El servicio de catering seleccionado no está disponible"
            );
        }

        if (solicitud.getMenu() == null
                || solicitud.getMenu().isBlank()) {

            throw new IllegalArgumentException(
                    "El menú solicitado es obligatorio"
            );
        }

        if (solicitud.getFecha() == null) {

            throw new IllegalArgumentException(
                    "La fecha del servicio es obligatoria"
            );
        }

        if (solicitud.getHora() == null) {

            throw new IllegalArgumentException(
                    "La hora del servicio es obligatoria"
            );
        }

        if (solicitud.getLugar() == null
                || solicitud.getLugar().isBlank()) {

            throw new IllegalArgumentException(
                    "El lugar del servicio es obligatorio"
            );
        }

        if (solicitud.getEstado() == null
                || solicitud.getEstado().isBlank()) {

            solicitud.setEstado("PENDIENTE");
        }

        String estadoNormalizado
                = solicitud.getEstado()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (!ESTADOS_PERMITIDOS.contains(
                estadoNormalizado
        )) {

            throw new IllegalArgumentException(
                    "El estado de la solicitud no es válido"
            );
        }

        solicitud.setMenu(
                solicitud.getMenu().trim()
        );

        solicitud.setLugar(
                solicitud.getLugar().trim()
        );

        solicitud.setEstado(
                estadoNormalizado
        );
    }
    
}
