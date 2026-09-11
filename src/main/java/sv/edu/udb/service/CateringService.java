package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
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
    public Page<ServicioCatering> listarServicios(String busqueda, Pageable pageable) {
        if (busqueda == null || busqueda.isBlank()) {
            return cateringRepository.findAll(pageable);
        }
        return cateringRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                busqueda, busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public ServicioCatering buscarPorId(Integer id) {
        return cateringRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el servicio de catering con ID: " + id));
    }

    @Transactional
    public ServicioCatering registrarServicio(ServicioCatering servicio) {
        validarCapacidades(servicio);
        return cateringRepository.save(servicio);
    }

    @Transactional
    public ServicioCatering actualizarServicio(Integer id, ServicioCatering datosServicio) {
        ServicioCatering servicio = buscarPorId(id);
        validarCapacidades(datosServicio);

        servicio.setNombre(datosServicio.getNombre());
        servicio.setDescripcion(datosServicio.getDescripcion());
        servicio.setPrecioPorPersona(datosServicio.getPrecioPorPersona());
        servicio.setCapacidadMinima(datosServicio.getCapacidadMinima());
        servicio.setCapacidadMaxima(datosServicio.getCapacidadMaxima());
        servicio.setEstado(datosServicio.getEstado());

        return cateringRepository.save(servicio);
    }

    @Transactional
    public void eliminarServicio(Integer id) {
        ServicioCatering servicio = buscarPorId(id);
        cateringRepository.delete(servicio);
    }

    private void validarCapacidades(ServicioCatering servicio) {
        if (servicio.getCapacidadMinima() != null && servicio.getCapacidadMaxima() != null) {
            if (servicio.getCapacidadMaxima() < servicio.getCapacidadMinima()) {
                throw new IllegalArgumentException("La capacidad máxima no puede ser menor a la mínima");
            }
        }
    }
}