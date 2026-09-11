package sv.edu.udb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "diplomado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diplomado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diplomado")
    private Integer idDiplomado;

    @NotBlank(message = "El nombre del diplomado es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @NotNull(message = "El cupo máximo es obligatorio")
    @Min(value = 1, message = "El cupo debe ser al menos 1")
    @Column(name = "cupo_maximo", nullable = false)
    private Integer cupoMaximo;

    @NotNull(message = "La fecha de inicio es requerida")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de finalización es requerida")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
    @Column(name = "costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 30, message = "El estado no puede superar los 30 caracteres")
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "ACTIVO";

    @NotNull(message = "Debe seleccionar una categoría")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @NotNull(message = "Debe seleccionar una modalidad")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_modalidad", nullable = false)
    private Modalidad modalidad;

    @NotNull(message = "Debe asignar un docente")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;
}
