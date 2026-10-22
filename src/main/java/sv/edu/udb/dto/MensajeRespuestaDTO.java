package sv.edu.udb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con un mensaje informativo")
public record MensajeRespuestaDTO(String mensaje) {
}
