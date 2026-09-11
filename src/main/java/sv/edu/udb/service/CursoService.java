package sv.edu.udb.service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Curso;
import sv.edu.udb.repository.CursoRepository;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor

public class CursoService {
    private final CursoRepository cursoRepository;

    @Transactional(readOnly = true)
    public Page<Curso> listarCursos(String busqueda, Pageable pageable) {
        if (busqueda == null || busqueda.isBlank()) {
            return cursoRepository.findAll(pageable);
        }
        return cursoRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                busqueda,
                busqueda,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Curso buscarPorId(Integer id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el curso con ID: " + id));
    }

    @Transactional
    public Curso registrarCurso(Curso curso) {
        validarFechas(curso);
        return cursoRepository.save(curso);
    }

    @Transactional
    public Curso actualizarCurso(Integer id, Curso datosCurso) {
        Curso curso = buscarPorId(id);
        validarFechas(datosCurso);

        curso.setNombre(datosCurso.getNombre());
        curso.setDescripcion(datosCurso.getDescripcion());
        curso.setCupoMaximo(datosCurso.getCupoMaximo());
        curso.setFechaInicio(datosCurso.getFechaInicio());
        curso.setFechaFin(datosCurso.getFechaFin());
        curso.setCosto(datosCurso.getCosto());
        curso.setEstado(datosCurso.getEstado());
        curso.setCategoria(datosCurso.getCategoria());
        curso.setModalidad(datosCurso.getModalidad());
        curso.setDocente(datosCurso.getDocente());

        return cursoRepository.save(curso);
    }

    @Transactional
    public void eliminarCurso(Integer id) {
        Curso curso = buscarPorId(id);
        cursoRepository.delete(curso);
    }

    private void validarFechas(Curso curso) {
        if (curso.getFechaInicio() == null || curso.getFechaFin() == null) return;
        if (curso.getFechaInicio().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser anterior a la fecha actual");
        }
        if (!curso.getFechaFin().isAfter(curso.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser estrictamente posterior a la fecha de inicio");
        }
    }
}
