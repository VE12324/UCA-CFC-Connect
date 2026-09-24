package sv.edu.udb.service;


import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Docente;
import sv.edu.udb.repository.DocenteRepository;


@Service
@RequiredArgsConstructor
public class DocenteService {


    private final DocenteRepository docenteRepositorio;


    @Transactional(readOnly = true)
    public Page<Docente> listarDocentes(String busqueda, Pageable paginacion) {


        if (busqueda == null || busqueda.isBlank()) {
            return docenteRepositorio.findAll(paginacion);
        }


        return docenteRepositorio
                .findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEspecialidadContainingIgnoreCase(
                        busqueda, busqueda, busqueda, paginacion
                );
    }


    @Transactional(readOnly = true)
    public Docente buscarPorId(Integer id) {
        return docenteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró el docente con ID: " + id
                ));
    }


    @Transactional
    public Docente registrarDocente(Docente docente) {
        validarNombreDuplicado(docente.getNombre(), docente.getApellido(), null);
        return docenteRepositorio.save(docente);
    }


    @Transactional
    public Docente actualizarDocente(Integer id, Docente datosDocente) {
        Docente docente = buscarPorId(id);


        validarNombreDuplicado(datosDocente.getNombre(), datosDocente.getApellido(), id);


        docente.setNombre(datosDocente.getNombre());
        docente.setApellido(datosDocente.getApellido());
        docente.setCorreo(datosDocente.getCorreo());
        docente.setTelefono(datosDocente.getTelefono());
        docente.setEspecialidad(datosDocente.getEspecialidad());


        return docenteRepositorio.save(docente);
    }


    @Transactional
    public void eliminarDocente(Integer id) {
        Docente docente = buscarPorId(id);


        try {
            docenteRepositorio.delete(docente);
            docenteRepositorio.flush();
        } catch (DataIntegrityViolationException excepcion) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el docente porque tiene cursos o diplomados asignados."
            );
        }
    }


    private void validarNombreDuplicado(String nombre, String apellido, Integer idActual) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            return;
        }


        docenteRepositorio.findByNombreIgnoreCaseAndApellidoIgnoreCase(nombre.trim(), apellido.trim())
                .filter(docente -> idActual == null || !docente.getIdDocente().equals(idActual))
                .ifPresent(docente -> {
                    throw new IllegalArgumentException(
                            "Ya existe un docente registrado con el nombre: " + nombre + " " + apellido
                    );
                });
    }
}


