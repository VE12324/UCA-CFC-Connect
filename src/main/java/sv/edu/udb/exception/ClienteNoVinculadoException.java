package sv.edu.udb.exception;

public class ClienteNoVinculadoException extends RuntimeException {

    public ClienteNoVinculadoException() {
        super("Tu cuenta aún no está vinculada a un cliente. "
                + "Comunícate con recepción.");
    }
}
