/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sv.edu.udb.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.EstadoInscripcion;
/**
 *
 * @author crist
 */
@Repository
public interface EstadoInscripcionRepository  extends JpaRepository<EstadoInscripcion, Integer> {
    
     Optional<EstadoInscripcion> findByNombreIgnoreCase(String nombre);
    
}
