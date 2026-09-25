package sv.edu.udb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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
    @Size(
            max = 80,
            message = "El nombre no puede superar los 80 caracteres"
    )
    @Column(
            name = "nombre",
            nullable = false,
            length = 80
    )
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(
            max = 100,
            message = "El correo no puede superar los 100 caracteres"
    )
    @Column(
            name = "correo",
            nullable = false,
            unique = true,
            length = 100
    )
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "La contraseña es obligatoria")
    @Column(
            name = "password",
            nullable = false,
            length = 255
    )
    private String password;

    @NotNull(message = "Debe indicar el rol del usuario")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_rol",
            nullable = false
    )
    private Rol rol;

    @NotNull
    @Column(
            name = "estado",
            nullable = false
    )
    private Boolean estado = true;

    public Usuario(
            Integer id,
            String nombre,
            String email,
            String password,
            Rol rol) {

        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.estado = true;
    }
}
