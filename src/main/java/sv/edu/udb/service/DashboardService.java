package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.model.Curso;
import sv.edu.udb.repository.CursoRepository;
import sv.edu.udb.repository.DiplomadoRepository;
import sv.edu.udb.repository.EspacioRepository;
import sv.edu.udb.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CursoRepository cursoRepository;
    private final DiplomadoRepository diplomadoRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public long countCursosActivos() {
        return cursoRepository.countByEstado("ACTIVO");
    }

    @Transactional(readOnly = true)
    public long countDiplomadosActivos() {
        return diplomadoRepository.countByEstado("ACTIVO");
    }

    @Transactional(readOnly = true)
    public long countEspaciosDisponibles() {
        return espacioRepository.countByEstado("DISPONIBLE");
    }

    @Transactional(readOnly = true)
    public long countUsuarios() {
        return usuarioRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Curso> listarUltimosCursos() {
        return cursoRepository.findAll(
                PageRequest.of(0, 5, Sort.by("idCurso").descending())
        ).getContent();
    }
}
