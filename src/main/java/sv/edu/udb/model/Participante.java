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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Table(name = "participante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participante {
        
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participante")
    private Integer idParticipante;

    @NotBlank(message = "El DUI es obligatorio")
    @Pattern(
            regexp = "\\d{8}-\\d",
            message = "El DUI debe tener el formato 00000000-0"
    )
    @Column(name = "dui", nullable = false, unique = true, length = 20)
    private String dui;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(
            max = 100,
            message = "El nombre no puede superar los 100 caracteres"
    )
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(
            max = 100,
            message = "El apellido no puede superar los 100 caracteres"
    )
    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Email(message = "El correo electrónico no es válido")
    @Size(
            max = 150,
            message = "El correo no puede superar los 150 caracteres"
    )
    @Column(name = "correo", length = 150)
    private String correo;

    @Size(
            max = 30,
            message = "El teléfono no puede superar los 30 caracteres"
    )
    @Column(name = "telefono", length = 30)
    private String telefono;
}
