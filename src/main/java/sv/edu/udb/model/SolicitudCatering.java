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
import java.time.LocalTime;
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
@Table(name = "solicitud_catering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCatering {
    
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @NotNull(message = "La cantidad de asistentes es obligatoria")
    @Min(
            value = 1,
            message = "La cantidad de asistentes debe ser al menos 1"
    )
    @Column(name = "cantidad_asistentes", nullable = false)
    private Integer cantidadAsistentes;

    @NotBlank(message = "El menú solicitado es obligatorio")
    @Size(
            max = 255,
            message = "El menú no puede superar los 255 caracteres"
    )
    @Column(name = "menu", nullable = false, length = 255)
    private String menu;

    @NotNull(message = "La fecha del servicio es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora del servicio es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El lugar del servicio es obligatorio")
    @Size(
            max = 255,
            message = "El lugar no puede superar los 255 caracteres"
    )
    @Column(name = "lugar", nullable = false, length = 255)
    private String lugar;

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

    @NotNull(message = "Debe seleccionar un servicio de catering")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_catering", nullable = false)
    private ServicioCatering servicioCatering;
    
}
