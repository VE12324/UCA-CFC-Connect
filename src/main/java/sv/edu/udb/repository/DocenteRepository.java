package sv.edu.udb.repository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Docente;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Integer> {
    // Validar duplicados por correo
    Optional<Docente> findByCorreo(String correo);
    boolean existsByCorreo(String correo);

    // Búsqueda por nombre, apellido o especialidad
    Page<Docente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEspecialidadContainingIgnoreCase(
            String nombre,
            String apellido,
            String especialidad,
            Pageable pageable
    );
}
