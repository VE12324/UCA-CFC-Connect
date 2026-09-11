package sv.edu.udb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(
            min = 3,
            max = 50,
            message = "El nombre debe tener entre 3 y 50 caracteres"
    )
    @Pattern(
            regexp = "^[A-ZÁÉÍÓÚÑ_ ]+$",
            message = "El nombre debe ir en mayúsculas, sin números (ej: ADMIN)"
    )
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Size(
            max = 150,
            message = "La descripción no puede superar los 150 caracteres"
    )
    @Column(name = "descripcion", length = 150)
    private String descripcion;


}
