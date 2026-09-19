package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    private final EspacioRepository espacioRepositorio;

    @Transactional(readOnly = true)
    public Page<Espacio> listarEspacios(
            String busqueda,
            Pageable paginacion) {

        if (busqueda == null || busqueda.isBlank()) {
            return espacioRepositorio.findAll(paginacion);
        }

        return espacioRepositorio
                .findByNombreContainingIgnoreCaseOrUbicacionContainingIgnoreCase(
                        busqueda,
                        busqueda,
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public Espacio buscarPorId(Integer id) {

        return espacioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró el espacio con ID: " + id
                ));
    }

    @Transactional
    public Espacio registrarEspacio(Espacio espacio) {

        validarNombreDuplicado(
                espacio.getNombre(),
                null
        );

        if (espacio.getEstado() == null
                || espacio.getEstado().isBlank()) {

            espacio.setEstado("DISPONIBLE");
        }

        return espacioRepositorio.save(espacio);
    }

    @Transactional
    public Espacio actualizarEspacio(
            Integer id,
            Espacio datosEspacio) {

        Espacio espacio = buscarPorId(id);

        validarNombreDuplicado(
                datosEspacio.getNombre(),
                id
        );

        espacio.setNombre(datosEspacio.getNombre());
        espacio.setDescripcion(datosEspacio.getDescripcion());
        espacio.setCapacidad(datosEspacio.getCapacidad());
        espacio.setUbicacion(datosEspacio.getUbicacion());
        espacio.setCostoHora(datosEspacio.getCostoHora());
        espacio.setEstado(datosEspacio.getEstado());

        return espacioRepositorio.save(espacio);
    }

    @Transactional
    public void eliminarEspacio(Integer id) {

        Espacio espacio = buscarPorId(id);

        try {

            espacioRepositorio.delete(espacio);
            espacioRepositorio.flush();

        } catch (DataIntegrityViolationException excepcion) {

            throw new IllegalArgumentException(
                    "No se puede eliminar el espacio porque tiene alquileres u otros registros asociados."
            );
        }
    }

    @Transactional(readOnly = true)
    public long contarEspaciosDisponibles() {

        return espacioRepositorio.countByEstado("DISPONIBLE");
    }

    private void validarNombreDuplicado(
            String nombre,
            Integer idEspacioActual) {

        if (nombre == null || nombre.isBlank()) {
            return;
        }

        espacioRepositorio.findByNombreIgnoreCase(nombre.trim())
                .filter(espacio ->
                        idEspacioActual == null
                        || !espacio.getIdEspacio()
                                .equals(idEspacioActual)
                )
                .ifPresent(espacio -> {

                    throw new IllegalArgumentException(
                            "Ya existe un espacio registrado con el nombre: "
                            + nombre
                    );
                });
    }
}