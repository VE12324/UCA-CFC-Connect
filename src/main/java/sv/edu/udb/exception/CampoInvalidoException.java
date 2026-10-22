package sv.edu.udb.exception;

import lombok.Getter;

@Getter
public class CampoInvalidoException extends IllegalArgumentException {

    private final String campo;

    public CampoInvalidoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }
}
