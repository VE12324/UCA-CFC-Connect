package sv.edu.udb.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Schema(description = "Cambio de contraseña. Enviar passwordActual (con token JWT) "
        + "o tokenRecuperacion (sin token JWT), además de passwordNueva.")
public class CambiarPasswordRequestDTO {


    @Schema(description = "Contraseña actual (opción A: usuario autenticado con JWT)")
    private String passwordActual;


    @Schema(description = "Token recibido en /api/auth/recuperar-password (opción B)")
    private String tokenRecuperacion;


    @Schema(description = "Nueva contraseña: mínimo 8 caracteres, una mayúscula, "
            + "una minúscula, un número y un carácter especial (@$!%*?&)",
            example = "Nueva@2026!")
    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String passwordNueva;
}

