package sv.edu.udb.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaOcupadaDTO(
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin) {
}
