package sv.edu.udb.dto;

import java.math.BigDecimal;


public record DetalleCotizacionRespuestaDTO(
        Integer idDetalle,
        String descripcion,
        Integer cantidad,
        BigDecimal precio,
        BigDecimal subtotal
) {
}

