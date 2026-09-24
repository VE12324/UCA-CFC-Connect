package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Categoria;
import sv.edu.udb.repository.CategoriaRepository;


@Service
@RequiredArgsConstructor
public class CategoriaService {


    private final CategoriaRepository categoriaRepositorio;


    @Transactional(readOnly = true)
    public Page<Categoria> listarCategorias(String busqueda, Pageable paginacion) {


        if (busqueda == null || busqueda.isBlank()) {
            return categoriaRepositorio.findAll(paginacion);
        }


        return categoriaRepositorio
                .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                        busqueda, busqueda, paginacion
                );
    }


    @Transactional(readOnly = true)
    public Categoria buscarPorId(Integer id) {
        return categoriaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró la categoría con ID: " + id
                ));
    }


    @Transactional
    public Categoria registrarCategoria(Categoria categoria) {
        validarNombreDuplicado(categoria.getNombre(), null);
        return categoriaRepositorio.save(categoria);
    }


    @Transactional
    public Categoria actualizarCategoria(Integer id, Categoria datosCategoria) {
        Categoria categoria = buscarPorId(id);


        validarNombreDuplicado(datosCategoria.getNombre(), id);


        categoria.setNombre(datosCategoria.getNombre());
        categoria.setDescripcion(datosCategoria.getDescripcion());


        return categoriaRepositorio.save(categoria);
    }


    @Transactional
    public void eliminarCategoria(Integer id) {
        Categoria categoria = buscarPorId(id);


        try {
            categoriaRepositorio.delete(categoria);
            categoriaRepositorio.flush();
        } catch (DataIntegrityViolationException excepcion) {
            throw new IllegalArgumentException(
                    "No se puede eliminar la categoría porque tiene cursos o diplomados asociados."
            );
        }
    }


    private void validarNombreDuplicado(String nombre, Integer idActual) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }


        categoriaRepositorio.findByNombreIgnoreCase(nombre.trim())
                .filter(categoria -> idActual == null || !categoria.getIdCategoria().equals(idActual))
                .ifPresent(categoria -> {
                    throw new IllegalArgumentException(
                            "Ya existe una categoría registrada con el nombre: " + nombre
                    );
                });
    }
}



