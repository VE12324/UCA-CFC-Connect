package sv.edu.udb.dto;

import java.math.BigDecimal;

public record ResumenPagosDTO(
        BigDecimal totalPagado,
        BigDecimal totalPorValidar,
        long cantidadPagos) {
}
