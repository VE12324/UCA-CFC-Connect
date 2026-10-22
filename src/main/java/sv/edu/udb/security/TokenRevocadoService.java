package sv.edu.udb.security;


import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;


/**
 * Lista en memoria de tokens JWT revocados por logout.
 *
 * Guarda el identificador (jti) de cada token hasta su fecha de expiración;
 * pasada esa fecha el token ya no es válido por sí mismo y se descarta de la
 * lista. Al reiniciar la aplicación la lista se pierde (no se guarda en BD).
 */
@Service
public class TokenRevocadoService {


    private final Map<String, Instant> revocados = new ConcurrentHashMap<>();


    public void revocar(String idToken, Instant expiracion) {


        limpiarExpirados();
        revocados.put(idToken, expiracion);
    }


    public boolean estaRevocado(String idToken) {


        if (idToken == null) {
            return false;
        }


        Instant expiracion = revocados.get(idToken);


        if (expiracion == null) {
            return false;
        }


        if (expiracion.isBefore(Instant.now())) {
            revocados.remove(idToken);
            return false;
        }


        return true;
    }


    private void limpiarExpirados() {


        Instant ahora = Instant.now();
        revocados.values().removeIf(expiracion -> expiracion.isBefore(ahora));
    }
}

