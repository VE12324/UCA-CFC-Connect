package sv.edu.udb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CotizacionRequestDTO {

    @NotNull(message = "Debe seleccionar un cliente")
    private Integer idCliente;


    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;


    @NotEmpty(message = "La cotización debe tener al menos un detalle")
    @Valid
    private List<DetalleCotizacionItemDTO> detalles;
}
