package sv.edu.udb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroClienteDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(
            max = 80,
            message = "El nombre no puede superar los 80 caracteres"
    )
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(
            max = 100,
            message = "El correo no puede superar los 100 caracteres"
    )
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)"
                    + "(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, "
                    + "una mayúscula, una minúscula, un número y "
                    + "un carácter especial (@$!%*?&)"
    )
    private String password;

    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmarPassword;
}
