package sv.edu.udb.security;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import sv.edu.udb.dto.RespuestaError;


/**
 * Respuestas 401 y 403 de la API en el mismo formato JSON que ManejoErrores
 * (RespuestaError). Se necesitan aquí porque esos errores ocurren en la
 * cadena de filtros de seguridad, antes de llegar a los controladores.
 */
@Component
@RequiredArgsConstructor
public class RespuestaErrorApi implements AuthenticationEntryPoint, AccessDeniedHandler {


    private final JsonMapper jsonMapper;


    /** 401: sin token, token inválido/expirado/revocado o cuenta deshabilitada. */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException excepcion) throws IOException {


        Object motivo = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERROR);


        escribir(
                response,
                HttpStatus.UNAUTHORIZED,
                "No autenticado",
                motivo != null
                        ? motivo.toString()
                        : "Se requiere un token JWT válido en el encabezado "
                          + "Authorization: Bearer <token>"
        );
    }


    /** 403: token válido pero el rol no tiene permiso para la ruta. */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException excepcion) throws IOException {


        escribir(
                response,
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                "Su rol no tiene permiso para acceder a este recurso"
        );
    }


    private void escribir(
            HttpServletResponse response,
            HttpStatus estado,
            String error,
            String mensaje) throws IOException {


        RespuestaError cuerpo = new RespuestaError(
                LocalDateTime.now(),
                estado.value(),
                error,
                mensaje,
                null
        );


        response.setStatus(estado.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), cuerpo);
    }
}

