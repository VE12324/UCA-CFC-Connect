package sv.edu.udb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {
    Page<Curso> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
            String nombre,
            String descripcion,
            Pageable pageable
    );
    long countByEstado(String estado);
}
