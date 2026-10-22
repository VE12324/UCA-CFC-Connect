package sv.edu.udb.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudAlquilerDTO {

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de finalización es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "La cantidad de personas debe ser al menos 1")
    private Integer cantidadPersonas;
}
