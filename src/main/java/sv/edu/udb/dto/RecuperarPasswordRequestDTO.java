package sv.edu.udb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Solicitud de recuperación de contraseña")
public class RecuperarPasswordRequestDTO {

    @Schema(example = "admin@uca.edu.sv")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;
}
