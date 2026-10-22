package sv.edu.udb.dto;


import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Token JWT emitido al iniciar sesión")
public record LoginRespuestaDTO(


        @Schema(description = "Token JWT para el encabezado Authorization")
        String token,


        @Schema(description = "Tipo de token", example = "Bearer")
        String tipo,


        @Schema(description = "Vigencia del token en segundos", example = "3600")
        long expiraEnSegundos,


        @Schema(description = "Correo de la cuenta", example = "admin@uca.edu.sv")
        String correo,


        @Schema(description = "Rol de la cuenta", example = "ADMIN")
        String rol
) {
}


