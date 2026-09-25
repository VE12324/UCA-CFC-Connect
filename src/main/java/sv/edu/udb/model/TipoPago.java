package sv.edu.udb.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Concepto del pago: indica qué se está pagando. El ID del registro pagado se guarda
 * en la columna de la tabla pago que corresponde a cada tipo.
 */
@Getter
@RequiredArgsConstructor
public enum TipoPago {


    INSCRIPCION("Inscripción"),
    COTIZACION("Cotización"),
    ALQUILER("Alquiler de espacio"),
    CATERING("Solicitud de catering");


    private final String etiqueta;
}
