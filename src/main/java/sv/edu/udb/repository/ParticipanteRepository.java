/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sv.edu.udb.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Participante;
/**
 *
 * @author crist
 */
@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {
    
    Optional<Participante> findByDui(String dui);

    boolean existsByDui(String dui);

    Page<Participante>
        findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrDuiContainingIgnoreCase
        (
                    String nombre,
                    String apellido,
                    String dui,
                    Pageable paginacion
        );
}
