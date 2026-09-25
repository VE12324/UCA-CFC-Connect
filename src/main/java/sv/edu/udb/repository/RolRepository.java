package sv.edu.udb.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    
     Optional<Rol> findByNombre(String nombre);
}
