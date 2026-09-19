package sv.edu.udb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "servicio_catering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioCatering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_catering")
    private Integer idCatering;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(
            max = 150,
            message = "El nombre no puede superar los 150 caracteres"
    )
    @Column(
            name = "nombre",
            nullable = false,
            length = 150
    )
    private String nombre;

    @Size(
            max = 500,
            message = "La descripción no puede superar los 500 caracteres"
    )
    @Column(
            name = "descripcion",
            length = 500
    )
    private String descripcion;

    @NotNull(message = "El precio por persona es obligatorio")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "El precio no puede ser negativo"
    )
    @Column(
            name = "precio_por_persona",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precioPorPersona;

    @NotNull(message = "La capacidad mínima es obligatoria")
    @Min(
            value = 1,
            message = "La capacidad mínima debe ser al menos 1"
    )
    @Column(
            name = "capacidad_minima",
            nullable = false
    )
    private Integer capacidadMinima;

    @NotNull(message = "La capacidad máxima es obligatoria")
    @Min(
            value = 1,
            message = "La capacidad máxima debe ser al menos 1"
    )
    @Column(
            name = "capacidad_maxima",
            nullable = false
    )
    private Integer capacidadMaxima;

    @NotBlank(message = "El estado es obligatorio")
    @Size(
            max = 30,
            message = "El estado no puede superar los 30 caracteres"
    )
    @Column(
            name = "estado",
            nullable = false,
            length = 30
    )
    private String estado = "DISPONIBLE";
}