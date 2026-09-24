package sv.edu.udb.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "estado_cotizacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoCotizacion {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_cotizacion")
    private Integer idEstadoCotizacion;


    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;
}
