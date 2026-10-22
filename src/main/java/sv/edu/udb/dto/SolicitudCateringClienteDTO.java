package sv.edu.udb.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class SolicitudCateringClienteDTO {

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    @NotBlank(message = "El lugar es obligatorio")
    @Size(max = 255, message = "El lugar no puede superar los 255 caracteres")
    private String lugar;

    @NotNull(message = "La cantidad de asistentes es obligatoria")
    @Min(value = 1, message = "La cantidad de asistentes debe ser al menos 1")
    private Integer cantidadAsistentes;

    @Size(max = 255, message = "El menú no puede superar los 255 caracteres")
    private String menu;
}
