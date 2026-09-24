package sv.edu.udb.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public record CotizacionRespuestaDTO(
        Integer idCotizacion,
        LocalDate fecha,
        BigDecimal subtotal,
        BigDecimal total,
        String estado,
        Integer idCliente,
        String nombreCliente,
        List<DetalleCotizacionRespuestaDTO> detalles
) {
}
