package sv.edu.udb.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import sv.edu.udb.model.TipoPago;


import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
public class PagoRequestDTO {


    @NotNull(message = "Debe seleccionar un cliente")
    private Integer idCliente;


    @NotNull(message = "Debe seleccionar un método de pago")
    private Integer idMetodoPago;


    @NotNull(message = "Debe indicar qué se está pagando")
    private TipoPago tipoPago;


    @NotNull(message = "Debe ingresar el ID del registro que se está pagando")
    @Min(value = 1, message = "El ID debe ser un número positivo")
    private Integer idReferencia;


    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;


    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;


    @Size(max = 150, message = "El número de referencia no puede superar los 150 caracteres")
    private String numeroReferenciaExterna;
}
