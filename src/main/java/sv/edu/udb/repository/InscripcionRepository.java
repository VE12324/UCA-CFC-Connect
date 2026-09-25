/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sv.edu.udb.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Inscripcion;
/**
 *
 * @author crist
 */
@Repository
public interface InscripcionRepository  extends JpaRepository<Inscripcion, Integer> {
    
    Page<Inscripcion>
            findByClienteNombreContainingIgnoreCaseOrCursoNombreContainingIgnoreCase(
                    String nombreCliente,
                    String nombreCurso,
                    Pageable paginacion
            );

    List<Inscripcion>
            findByCliente_IdClienteOrderByFechaDesc(
                    Integer idCliente
            );

    Page<Inscripcion>
            findByCliente_Usuario_EmailIgnoreCase(
                    String correo,
                    Pageable paginacion
            );
}
