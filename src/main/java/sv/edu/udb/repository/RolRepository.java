package sv.edu.udb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    java.util.Optional<Rol> findByNombre(String nombre);
}
