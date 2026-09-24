package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.repository.CateringRepository;

@Service
@RequiredArgsConstructor
public class CateringService {

    private final CateringRepository cateringRepository;

    @Transactional(readOnly = true)
    public Page<ServicioCatering> listarServicios(
            String busqueda,
            Pageable paginacion) {

        if (busqueda == null || busqueda.isBlank()) {
            return cateringRepository.findAll(paginacion);
        }

        return cateringRepository
                .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                        busqueda,
                        busqueda,
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public ServicioCatering buscarPorId(Integer id) {

        return cateringRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró el servicio de catering con ID: "
                        + id
                ));
    }

    @Transactional
    public ServicioCatering registrarServicio(
            ServicioCatering servicio) {

        validarCapacidades(servicio);

        if (servicio.getEstado() == null
                || servicio.getEstado().isBlank()) {

            servicio.setEstado("DISPONIBLE");
        }

        return cateringRepository.save(servicio);
    }

    @Transactional
    public ServicioCatering actualizarServicio(
            Integer id,
            ServicioCatering datosServicio) {

        ServicioCatering servicio =
                buscarPorId(id);

        validarCapacidades(datosServicio);

        servicio.setNombre(
                datosServicio.getNombre()
        );

        servicio.setDescripcion(
                datosServicio.getDescripcion()
        );

        servicio.setPrecioPorPersona(
                datosServicio.getPrecioPorPersona()
        );

        servicio.setCapacidadMinima(
                datosServicio.getCapacidadMinima()
        );

        servicio.setCapacidadMaxima(
                datosServicio.getCapacidadMaxima()
        );

        servicio.setEstado(
                datosServicio.getEstado()
        );

        return cateringRepository.save(servicio);
    }

    @Transactional
    public void eliminarServicio(Integer id) {

        ServicioCatering servicio =
                buscarPorId(id);

        try {

            cateringRepository.delete(servicio);
            cateringRepository.flush();

        } catch (DataIntegrityViolationException excepcion) {

            throw new IllegalArgumentException(
                    "No se puede eliminar el servicio de catering porque tiene solicitudes asociadas."
            );
        }
    }

    private void validarCapacidades(
            ServicioCatering servicio) {

        if (servicio.getCapacidadMinima() != null
                && servicio.getCapacidadMaxima() != null
                && servicio.getCapacidadMaxima()
                        < servicio.getCapacidadMinima()) {

            throw new IllegalArgumentException(
                    "La capacidad máxima no puede ser menor que la capacidad mínima."
            );
        }
    }
}