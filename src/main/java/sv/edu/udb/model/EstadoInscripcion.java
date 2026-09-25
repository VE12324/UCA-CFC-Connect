/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 *
 * @author crist
 */

@Entity
@Table(name = "estado_inscripcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoInscripcion {
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_inscripcion")
    private Integer idEstadoInscripcion;

    @NotBlank(message = "El nombre del estado es obligatorio")
    @Size(
            max = 50,
            message = "El nombre del estado no puede superar los 50 caracteres"
    )
    @Column(
            name = "nombre",
            nullable = false,
            unique = true,
            length = 50
    )
    private String nombre;
    
}
