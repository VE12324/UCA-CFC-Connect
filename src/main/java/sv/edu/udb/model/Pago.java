package sv.edu.udb.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pago {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;


    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;


    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;


    // Número de referencia bancaria o de autorización (tarjeta, transferencia, depósito).
    // Se mantiene el nombre de columna original "referencia" del esquema real.
    @Size(max = 150, message = "El número de referencia no puede superar los 150 caracteres")
    @Column(name = "referencia", length = 150)
    private String numeroReferenciaExterna;


    // Concepto del pago. Columna nueva agregada vía ddl-auto=update; es nullable en BD porque
    // los pagos registrados antes de existir este campo no la tienen (se infiere al leerlos).
    // Se guarda como VARCHAR: con un ENUM nativo de MariaDB, ddl-auto=update re-ejecuta un
    // ALTER TABLE en cada arranque.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo_pago", length = 20)
    private TipoPago tipoPago;


    @NotNull(message = "El estado del pago es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado_pago", nullable = false)
    private EstadoPago estadoPago;


    @NotNull(message = "Debe seleccionar un cliente")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;


    @NotNull(message = "Debe seleccionar un método de pago")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPago metodoPago;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cotizacion")
    private Cotizacion cotizacion;


    // Columnas heredadas del esquema real que aún no tienen módulo/entidad propia
    // (inscripción, alquiler y solicitud de catering son responsabilidad de Recepcionista).
    // Se mapean como IDs planos, sin relación JPA, para no invadir esos módulos.
    // Guardan el idReferencia del pago según su tipoPago (la FK de la BD valida que exista).
    @Column(name = "id_inscripcion")
    private Integer idInscripcion;


    @Column(name = "id_alquiler")
    private Integer idAlquiler;


    @Column(name = "id_solicitud_catering")
    private Integer idSolicitudCatering;


    // Columna nueva (no existente en el dump original), agregada vía ddl-auto=update
    // para soportar la emisión de comprobante sin requerir un módulo de PDF real.
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;
}
