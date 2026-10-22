package sv.edu.udb.exception;


/**
 * Se lanza cuando un alquiler se traslapa con otro alquiler no cancelado
 * del mismo espacio.
 *
 * Extiende IllegalArgumentException para que los controladores web, que ya
 * capturan IllegalArgumentException, sigan mostrando el mensaje en el
 * formulario. ManejoErrores la traduce a 409 (Conflict) en la API.
 */
public class EspacioOcupadoException extends IllegalArgumentException {


    public EspacioOcupadoException(String mensaje) {
        super(mensaje);
    }
}


