package sv.edu.udb.model;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "metodo_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPago {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pago")
    private Integer idMetodoPago;


    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;
}
