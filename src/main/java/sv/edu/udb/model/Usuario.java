package sv.edu.udb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;


    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    @Column(name = "correo", nullable = false, unique = true, length = 100)
    private String email;


    @NotBlank(message = "El password es obligatorio")
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull(message = "Debe indicar el rol del usuario")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

}
