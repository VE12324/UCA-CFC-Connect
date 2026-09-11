package sv.edu.udb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "docente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Docente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_docente")
    private Integer idDocente;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Email(message = "El correo electrónico no es válido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    @Column(name = "correo", length = 150)
    private String correo;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    @Column(name = "telefono", length = 30)
    private String telefono;

    @Size(max = 150, message = "La especialidad no puede superar los 150 caracteres")
    @Column(name = "especialidad", length = 150)
    private String especialidad;
}
