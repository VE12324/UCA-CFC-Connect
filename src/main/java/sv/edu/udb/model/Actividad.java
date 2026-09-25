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
@Table(name = "actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {
    
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    private Integer idActividad;

    @NotBlank(message = "El nombre de la actividad es obligatorio")
    @Size(
            max = 150,
            message = "El nombre no puede superar los 150 caracteres"
    )
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "El tipo de actividad es obligatorio")
    @Size(
            max = 100,
            message = "El tipo no puede superar los 100 caracteres"
    )
    @Column(name = "tipo", nullable = false, length = 100)
    private String tipo = "INSTITUCIONAL";

    @NotNull(message = "La fecha de la actividad es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Size(
            max = 100,
            message = "El horario no puede superar los 100 caracteres"
    )
    @Column(name = "horario", length = 100)
    private String horario;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "hora_inicio")
    private LocalDateTime horaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "hora_fin")
    private LocalDateTime horaFin;

    @Size(
            max = 500,
            message = "La descripción no puede superar los 500 caracteres"
    )
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_curso")
    private Curso curso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_diplomado")
    private Diplomado diplomado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_alquiler")
    private Alquiler alquiler;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_solicitud_catering")
    private SolicitudCatering solicitudCatering;
    
}
