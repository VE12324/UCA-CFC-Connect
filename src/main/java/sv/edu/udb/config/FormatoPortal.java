package sv.edu.udb.config;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.model.ServicioCatering;

@Component("formatoPortal")
public class FormatoPortal {


    private static final String[] MESES_CORTOS = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };


    private static final String[] MESES = {
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    };


    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");


    /** "15 de Oct" */
    public String fechaCorta(LocalDate fecha) {


        if (fecha == null) {
            return "Por definir";
        }


        return String.format("%02d de %s",
                fecha.getDayOfMonth(),
                MESES_CORTOS[fecha.getMonthValue() - 1]);
    }


    /** "15 de octubre de 2026" */
    public String fechaLarga(LocalDate fecha) {


        if (fecha == null) {
            return "Por definir";
        }


        return fecha.getDayOfMonth() + " de "
                + MESES[fecha.getMonthValue() - 1]
                + " de " + fecha.getYear();
    }


    public String hora(LocalTime hora) {
        return hora == null ? "" : hora.format(HORA);
    }


    public String hora(LocalDateTime fechaHora) {
        return fechaHora == null ? "" : fechaHora.format(HORA);
    }


    /** "$1,250.00" */
    public String dinero(BigDecimal monto) {


        BigDecimal valor = monto == null ? BigDecimal.ZERO : monto;


        return String.format(Locale.US, "$%,.2f",
                valor.setScale(2, RoundingMode.HALF_UP));
    }


    /** Texto con mayúscula inicial: "PENDIENTE" → "Pendiente". */
    public String estado(String estado) {


        if (estado == null || estado.isBlank()) {
            return "Sin estado";
        }


        String texto = estado.trim().toLowerCase(Locale.ROOT);


        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }


    /** Clase CSS de la insignia de estado (colores del portal). */
    public String claseEstado(String estado) {


        if (estado == null) {
            return "insignia insignia-neutra";
        }


        String texto = estado.trim().toUpperCase(Locale.ROOT);


        if (texto.startsWith("PENDIENTE") || texto.equals("EN PROCESO")
                || texto.equals("PARCIAL")) {
            return "insignia insignia-pendiente";
        }


        if (texto.startsWith("CONFIRMAD") || texto.equals("APROBADA")
                || texto.equals("PAGADO") || texto.equals("DISPONIBLE")) {
            return "insignia insignia-exito";
        }


        if (texto.startsWith("CANCELAD") || texto.equals("RECHAZADA")
                || texto.equals("MANTENIMIENTO")
                || texto.equals("NO DISPONIBLE")) {
            return "insignia insignia-peligro";
        }


        return "insignia insignia-neutra";
    }


    /** Mismo criterio que AlquilerService: solo MANTENIMIENTO bloquea la reserva. */
    public boolean espacioReservable(Espacio espacio) {
        return !"MANTENIMIENTO".equalsIgnoreCase(espacio.getEstado());
    }


    /** Mismo criterio que SolicitudCateringService: solo DISPONIBLE se puede pedir. */
    public boolean cateringDisponible(ServicioCatering servicio) {
        return "DISPONIBLE".equalsIgnoreCase(servicio.getEstado());
    }
}


