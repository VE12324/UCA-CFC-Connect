package sv.edu.udb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "espacio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_espacio")
    private Integer idEspacio;

    @NotBlank(message = "El nombre del espacio es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Size(max = 255, message = "La ubicación no puede superar los 255 caracteres")
    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    @NotNull(message = "El costo por hora es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
    @Column(name = "costo_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoHora;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 30, message = "El estado no puede superar los 30 caracteres")
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "DISPONIBLE";
}
