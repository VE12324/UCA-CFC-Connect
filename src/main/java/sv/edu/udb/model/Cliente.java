/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author crist
 */
@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Size(
            max = 20,
            message = "El DUI no puede superar los 20 caracteres"
    )
    @Column(name = "dui", length = 20)
    private String dui;

    @NotBlank(message = "El nombre es obligatorio")
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
            max = 150,
            message = "La empresa no puede superar los 150 caracteres"
    )
    @Column(name = "empresa", length = 150)
    private String empresa;

    @Email(message = "El correo electrónico no es válido")
    @Size(
            max = 150,
            message = "El correo no puede superar los 150 caracteres"
    )
    @Column(name = "correo", length = 150)
    private String correo;

    @Size(
            max = 30,
            message = "El teléfono no puede superar los 30 caracteres"
    )
    @Column(name = "telefono", length = 30)
    private String telefono;

    @Size(
            max = 255,
            message = "La dirección no puede superar los 255 caracteres"
    )
    @Column(name = "direccion", length = 255)
    private String direccion;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_usuario",
            unique = true
    )
    private Usuario usuario;

    @Transient
    private Boolean crearCuenta = false;

    @JsonIgnore
    @Transient
    private String password;

    @JsonIgnore
    @Transient
    private String confirmarPassword;

    public Cliente(
            Integer idCliente,
            String dui,
            String nombre,
            String empresa,
            String correo,
            String telefono,
            String direccion) {

        this.idCliente = idCliente;
        this.dui = dui;
        this.nombre = nombre;
        this.empresa = empresa;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.crearCuenta = false;
    }
}
