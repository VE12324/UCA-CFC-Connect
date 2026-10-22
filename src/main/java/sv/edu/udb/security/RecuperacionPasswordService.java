package sv.edu.udb.security;


import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sv.edu.udb.repository.UsuarioRepository;


/**
 * Tokens de recuperación de contraseña de un solo uso, guardados en memoria
 * (sin cambios en el esquema de la BD).
 *
 * Como el proyecto no envía correos, el token se escribe en la consola del
 * servidor simulando el envío.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecuperacionPasswordService {


    public static final Duration VIGENCIA = Duration.ofMinutes(15);


    private final UsuarioRepository usuarioRepository;


    private final SecureRandom aleatorio = new SecureRandom();


    private final Map<String, Solicitud> solicitudes = new ConcurrentHashMap<>();


    private record Solicitud(String correo, Instant expiracion) {
    }


    /**
     * Genera un token si el correo pertenece a una cuenta activa. Si no
     * existe, no hace nada: el controlador responde lo mismo en ambos casos
     * para no revelar qué correos están registrados.
     */
    public void solicitar(String correo) {


        limpiarExpirados();


        if (correo == null || correo.isBlank()) {
            return;
        }


        usuarioRepository.findByEmail(correo.trim().toLowerCase())
                .filter(usuario -> Boolean.TRUE.equals(usuario.getEstado()))
                .ifPresent(usuario -> {


                    byte[] bytes = new byte[32];
                    aleatorio.nextBytes(bytes);


                    String token = Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(bytes);


                    solicitudes.put(
                            token,
                            new Solicitud(
                                    usuario.getEmail(),
                                    Instant.now().plus(VIGENCIA)
                            )
                    );


                    log.warn(
                            "[SIMULACIÓN DE CORREO] Para: {} | Token de recuperación "
                                    + "(válido 15 minutos, un solo uso): {}",
                            usuario.getEmail(),
                            token
                    );
                });
    }


    /**
     * Valida el token sin consumirlo, para no gastarlo si la nueva
     * contraseña no cumple la política.
     *
     * @return el correo de la cuenta asociada
     * @throws IllegalArgumentException si el token no existe, ya se usó o expiró
     */
    public String obtenerCorreo(String token) {


        Solicitud solicitud = token == null ? null : solicitudes.get(token);


        if (solicitud == null
                || solicitud.expiracion().isBefore(Instant.now())) {


            throw new IllegalArgumentException(
                    "El token de recuperación no es válido o ya expiró"
            );
        }


        return solicitud.correo();
    }


    /**
     * Marca el token como usado (un solo uso).
     */
    public void invalidar(String token) {


        if (token != null) {
            solicitudes.remove(token);
        }
    }


    private void limpiarExpirados() {


        Instant ahora = Instant.now();
        solicitudes.values().removeIf(
                solicitud -> solicitud.expiracion().isBefore(ahora)
        );
    }
}

