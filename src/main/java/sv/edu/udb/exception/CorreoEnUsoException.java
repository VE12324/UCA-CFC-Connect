package sv.edu.udb.exception;

public class CorreoEnUsoException extends IllegalArgumentException {

    public CorreoEnUsoException(String mensaje) {
        super(mensaje);
    }
}
