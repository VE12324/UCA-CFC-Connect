package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.repository.EspacioRepository;

@Service
@RequiredArgsConstructor
public class EspacioService {

    private final EspacioRepository espacioRepository;

    @Transactional(readOnly = true)
    public Page<Espacio> listarEspacios(String busqueda, Pageable pageable) {
        if (busqueda == null || busqueda.isBlank()) {
            return espacioRepository.findAll(pageable);
        }
        return espacioRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                busqueda, busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public Espacio buscarPorId(Integer id) {
        return espacioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el espacio con ID: " + id));
    }

    @Transactional
    public Espacio registrarEspacio(Espacio espacio) {
        return espacioRepository.save(espacio);
    }

    @Transactional
    public Espacio actualizarEspacio(Integer id, Espacio datosEspacio) {
        Espacio espacio = buscarPorId(id);

        espacio.setNombre(datosEspacio.getNombre());
        espacio.setDescripcion(datosEspacio.getDescripcion());
        espacio.setCapacidad(datosEspacio.getCapacidad());
        espacio.setUbicacion(datosEspacio.getUbicacion());
        espacio.setCostoHora(datosEspacio.getCostoHora());
        espacio.setEstado(datosEspacio.getEstado());

        return espacioRepository.save(espacio);
    }

    @Transactional
    public void eliminarEspacio(Integer id) {
        Espacio espacio = buscarPorId(id);
        espacioRepository.delete(espacio);
    }
}
