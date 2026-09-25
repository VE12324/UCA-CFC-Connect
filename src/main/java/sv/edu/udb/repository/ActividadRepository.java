/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sv.edu.udb.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Actividad;
/**
 *
 * @author crist
 */
@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer>{
    
    Page<Actividad>
            findByNombreContainingIgnoreCaseOrTipoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                    String nombre,
                    String tipo,
                    String descripcion,
                    Pageable paginacion
            );

    @Query("""
           SELECT DISTINCT a
           FROM Actividad a
           WHERE
               (
                   a.curso IS NOT NULL
                   AND EXISTS (
                       SELECT i.idInscripcion
                       FROM Inscripcion i
                       WHERE i.cliente.usuario.email = :correo
                         AND i.curso.idCurso = a.curso.idCurso
                   )
               )
               OR (
                   a.alquiler IS NOT NULL
                   AND a.alquiler.cliente.usuario.email = :correo
               )
               OR (
                   a.solicitudCatering IS NOT NULL
                   AND a.solicitudCatering.cliente.usuario.email = :correo
               )
               OR (
                   a.curso IS NULL
                   AND a.diplomado IS NULL
                   AND a.alquiler IS NULL
                   AND a.solicitudCatering IS NULL
               )
           ORDER BY a.fecha DESC, a.horaInicio ASC
           """)
    List<Actividad> buscarAgendaDelCliente(
            @Param("correo")
            String correo
    );
     
}
