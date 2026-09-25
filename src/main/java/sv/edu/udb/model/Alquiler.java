/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
/**
 *
 * @author crist
 */
@Entity
@Table(name = "alquiler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alquiler {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alquiler")
    private Integer idAlquiler;

    @NotNull(message = "La fecha del alquiler es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "hora_inicio", nullable = false)
    private LocalDateTime horaInicio;

    @NotNull(message = "La fecha y hora de finalización son obligatorias")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "hora_fin", nullable = false)
    private LocalDateTime horaFin;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(
            value = 1,
            message = "La cantidad de personas debe ser al menos 1"
    )
    @Column(name = "cantidad_personas", nullable = false)
    private Integer cantidadPersonas;

    @NotBlank(message = "El estado es obligatorio")
    @Size(
            max = 30,
            message = "El estado no puede superar los 30 caracteres"
    )
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @NotNull(message = "Debe seleccionar un cliente")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @NotNull(message = "Debe seleccionar un espacio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_espacio", nullable = false)
    private Espacio espacio;
    
}
