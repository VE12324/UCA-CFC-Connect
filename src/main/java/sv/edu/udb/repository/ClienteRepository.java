
package sv.edu.udb.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer>{

    Optional<Cliente> findByDui(String dui);


    boolean existsByDui(String dui);


    Page<Cliente>
    findByNombreContainingIgnoreCaseOrDuiContainingIgnoreCaseOrEmpresaContainingIgnoreCase(
            String nombre,
            String dui,
            String empresa,
            Pageable pageable
    );


    Optional<Cliente> findByUsuario_Id(
            Integer idUsuario
    );


    Optional<Cliente> findByUsuario_EmailIgnoreCase(
            String correo
    );


    Optional<Cliente>
    findFirstByCorreoIgnoreCaseAndUsuarioIsNullOrderByIdClienteAsc(
            String correo
    );
}
