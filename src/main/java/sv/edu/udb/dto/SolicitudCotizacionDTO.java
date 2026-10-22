package sv.edu.udb.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Solicitud de cotización del cliente: solo la lista de líneas. */
@Getter
@Setter
public class SolicitudCotizacionDTO {

    private List<LineaCotizacionClienteDTO> lineas = new ArrayList<>();
}
