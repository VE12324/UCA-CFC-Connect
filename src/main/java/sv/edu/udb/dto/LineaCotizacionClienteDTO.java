package sv.edu.udb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineaCotizacionClienteDTO {

    private String tipo;

    private Integer idItem;

    private Integer cantidad;

    private String descripcion;
}
