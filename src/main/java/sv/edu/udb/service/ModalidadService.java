package sv.edu.udb.service;


import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Modalidad;
import sv.edu.udb.repository.ModalidadRepository;


@Service
@RequiredArgsConstructor
public class ModalidadService {


    private final ModalidadRepository modalidadRepositorio;


    @Transactional(readOnly = true)
    public Page<Modalidad> listarModalidades(String busqueda, Pageable paginacion) {


        if (busqueda == null || busqueda.isBlank()) {
            return modalidadRepositorio.findAll(paginacion);
        }


        return modalidadRepositorio
                .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                        busqueda, busqueda, paginacion
                );
    }


    @Transactional(readOnly = true)
    public Modalidad buscarPorId(Integer id) {
        return modalidadRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró la modalidad con ID: " + id
                ));
    }


    @Transactional
    public Modalidad registrarModalidad(Modalidad modalidad) {
        validarNombreDuplicado(modalidad.getNombre(), null);
        return modalidadRepositorio.save(modalidad);
    }


    @Transactional
    public Modalidad actualizarModalidad(Integer id, Modalidad datosModalidad) {
        Modalidad modalidad = buscarPorId(id);


        validarNombreDuplicado(datosModalidad.getNombre(), id);


        modalidad.setNombre(datosModalidad.getNombre());
        modalidad.setDescripcion(datosModalidad.getDescripcion());


        return modalidadRepositorio.save(modalidad);
    }


    @Transactional
    public void eliminarModalidad(Integer id) {
        Modalidad modalidad = buscarPorId(id);


        try {
            modalidadRepositorio.delete(modalidad);
            modalidadRepositorio.flush();
        } catch (DataIntegrityViolationException excepcion) {
            throw new IllegalArgumentException(
                    "No se puede eliminar la modalidad porque tiene cursos o diplomados asociados."
            );
        }
    }


    private void validarNombreDuplicado(String nombre, Integer idActual) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }


        modalidadRepositorio.findByNombreIgnoreCase(nombre.trim())
                .filter(modalidad -> idActual == null || !modalidad.getIdModalidad().equals(idActual))
                .ifPresent(modalidad -> {
                    throw new IllegalArgumentException(
                            "Ya existe una modalidad registrada con el nombre: " + nombre
                    );
                });
    }
}


