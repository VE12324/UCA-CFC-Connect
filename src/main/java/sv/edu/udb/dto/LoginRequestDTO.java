package sv.edu.udb.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Schema(description = "Credenciales para obtener un token JWT")
public class LoginRequestDTO {


    @Schema(example = "admin@uca.edu.sv")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    private String correo;


    @Schema(example = "MiClave@2026")
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}

