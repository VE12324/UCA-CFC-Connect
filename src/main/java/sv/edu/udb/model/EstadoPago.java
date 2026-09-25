package sv.edu.udb.model;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "estado_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoPago {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_pago")
    private Integer idEstadoPago;


    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;
}
