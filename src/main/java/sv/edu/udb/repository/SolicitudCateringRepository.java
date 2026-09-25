/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sv.edu.udb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.SolicitudCatering;
/**
 *
 * @author crist
 */
@Repository
public interface SolicitudCateringRepository  extends JpaRepository<SolicitudCatering, Integer>{
    
     Page<SolicitudCatering>
            findByClienteNombreContainingIgnoreCaseOrServicioCateringNombreContainingIgnoreCaseOrLugarContainingIgnoreCase(
                    String nombreCliente,
                    String nombreServicio,
                    String lugar,
                    Pageable paginacion
            );

    Page<SolicitudCatering>
            findByCliente_Usuario_EmailIgnoreCase(
                    String correo,
                    Pageable paginacion
            );
}
