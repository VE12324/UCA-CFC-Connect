package sv.edu.udb.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import sv.edu.udb.model.Curso;
import sv.edu.udb.model.Diplomado;

public record ProgramaPortalDTO(
        String tipo,
        Integer id,
        String nombre,
        String descripcion,
        Integer idCategoria,
        String categoria,
        Integer idModalidad,
        String modalidad,
        String docente,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer cupoMaximo,
        BigDecimal costo) {

    public static final String CURSO = "CURSO";
    public static final String DIPLOMADO = "DIPLOMADO";

    public static ProgramaPortalDTO desde(Curso curso) {

        return new ProgramaPortalDTO(
                CURSO,
                curso.getIdCurso(),
                curso.getNombre(),
                curso.getDescripcion(),
                curso.getCategoria() != null ? curso.getCategoria().getIdCategoria() : null,
                curso.getCategoria() != null ? curso.getCategoria().getNombre() : null,
                curso.getModalidad() != null ? curso.getModalidad().getIdModalidad() : null,
                curso.getModalidad() != null ? curso.getModalidad().getNombre() : null,
                curso.getDocente() != null
                        ? curso.getDocente().getNombre() + " " + curso.getDocente().getApellido()
                        : null,
                curso.getFechaInicio(),
                curso.getFechaFin(),
                curso.getCupoMaximo(),
                curso.getCosto()
        );
    }

    public static ProgramaPortalDTO desde(Diplomado diplomado) {

        return new ProgramaPortalDTO(
                DIPLOMADO,
                diplomado.getIdDiplomado(),
                diplomado.getNombre(),
                diplomado.getDescripcion(),
                diplomado.getCategoria() != null ? diplomado.getCategoria().getIdCategoria() : null,
                diplomado.getCategoria() != null ? diplomado.getCategoria().getNombre() : null,
                diplomado.getModalidad() != null ? diplomado.getModalidad().getIdModalidad() : null,
                diplomado.getModalidad() != null ? diplomado.getModalidad().getNombre() : null,
                diplomado.getDocente() != null
                        ? diplomado.getDocente().getNombre() + " " + diplomado.getDocente().getApellido()
                        : null,
                diplomado.getFechaInicio(),
                diplomado.getFechaFin(),
                diplomado.getCupoMaximo(),
                diplomado.getCosto()
        );
    }

    public boolean esCurso() {
        return CURSO.equals(tipo);
    }

    /** Ruta del detalle en el sitio público. */
    public String ruta() {
        return "/portal/oferta/" + (esCurso() ? "curso/" : "diplomado/") + id;
    }

    /** Parámetro para precargar la solicitud de cotización. */
    public String parametroCotizacion() {
        return (esCurso() ? "curso=" : "diplomado=") + id;
    }
}
