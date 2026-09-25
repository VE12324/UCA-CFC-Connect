/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sv.edu.udb.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Alquiler;
/**
 *
 * @author crist
 */
public interface AlquilerRepository extends JpaRepository<Alquiler, Integer>{
    
     Page<Alquiler>
            findByClienteNombreContainingIgnoreCaseOrEspacioNombreContainingIgnoreCase(
                    String nombreCliente,
                    String nombreEspacio,
                    Pageable paginacion
            );

    Page<Alquiler>
            findByCliente_Usuario_EmailIgnoreCase(
                    String correo,
                    Pageable paginacion
            );

    @Query("""
           SELECT COUNT(a) > 0
           FROM Alquiler a
           WHERE a.espacio.idEspacio = :idEspacio
             AND UPPER(a.estado) <> 'CANCELADO'
             AND (:idAlquiler IS NULL
                  OR a.idAlquiler <> :idAlquiler)
             AND a.horaInicio < :horaFin
             AND a.horaFin > :horaInicio
           """)
    boolean existeConflictoHorario(
            @Param("idEspacio")
            Integer idEspacio,
            @Param("horaInicio")
            LocalDateTime horaInicio,
            @Param("horaFin")
            LocalDateTime horaFin,
            @Param("idAlquiler")
            Integer idAlquiler
    );
}
