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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
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
@Table(name = "inscripcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {
    
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Integer idInscripcion;

    @NotNull(message = "La fecha de inscripción es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "Debe seleccionar un estado")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado_inscripcion", nullable = false)
    private EstadoInscripcion estadoInscripcion;

    @NotNull(message = "Debe seleccionar un cliente")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @NotNull(message = "Debe seleccionar un curso")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "inscripcion_participante",
            joinColumns = @JoinColumn(name = "id_inscripcion"),
            inverseJoinColumns = @JoinColumn(name = "id_participante")
    )
    private Set<Participante> participantes = new LinkedHashSet<>();
    
}
