package sv.edu.udb.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class JwtService {


    public static final String CLAIM_ROL = "rol";


    private static final int LONGITUD_MINIMA_SECRETO = 32;


    private final SecretKey clave;
    private final long expiracionMs;


    public JwtService(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiration-ms}") long expiracionMs) {


        if (secreto == null
                || secreto.getBytes(StandardCharsets.UTF_8).length
                < LONGITUD_MINIMA_SECRETO) {


            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos "
                            + LONGITUD_MINIMA_SECRETO + " caracteres"
            );
        }


        this.clave = Keys.hmacShaKeyFor(
                secreto.getBytes(StandardCharsets.UTF_8)
        );
        this.expiracionMs = expiracionMs;
    }


    public String generarToken(String correo, String rol) {


        Date ahora = new Date();


        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(correo)
                .claim(CLAIM_ROL, rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }



    public Claims validarYObtenerClaims(String token) {


        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public boolean esTokenValido(String token) {


        if (token == null || token.isBlank()) {
            return false;
        }


        try {
            validarYObtenerClaims(token);
            return true;


        } catch (JwtException | IllegalArgumentException excepcion) {
            return false;
        }
    }


    public String extraerCorreo(String token) {


        return validarYObtenerClaims(token).getSubject();
    }


    public long getExpiracionMs() {


        return expiracionMs;
    }
}

