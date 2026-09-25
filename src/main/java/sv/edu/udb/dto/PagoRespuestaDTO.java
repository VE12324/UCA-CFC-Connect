package sv.edu.udb.dto;


import sv.edu.udb.model.TipoPago;


import java.math.BigDecimal;
import java.time.LocalDate;


public record PagoRespuestaDTO(
        Integer idPago,
        LocalDate fecha,
        BigDecimal monto,
        TipoPago tipoPago,
        Integer idReferencia,
        String numeroReferenciaExterna,
        String estado,
        Integer idMetodoPago,
        String metodoPago,
        Integer idCliente,
        String nombreCliente,
        Integer idCotizacion,
        LocalDate fechaEmision
) {
}
