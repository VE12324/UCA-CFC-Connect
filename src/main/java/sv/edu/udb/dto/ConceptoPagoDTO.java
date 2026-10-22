package sv.edu.udb.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import sv.edu.udb.model.TipoPago;

public record ConceptoPagoDTO(
        TipoPago tipoPago,
        String etiquetaTipo,
        Integer idReferencia,
        Integer idCliente,
        String descripcion,
        BigDecimal total,
        BigDecimal pagado,
        BigDecimal saldo,
        @JsonIgnore boolean vigente
) {
}
