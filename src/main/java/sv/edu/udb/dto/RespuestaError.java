/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.dto;

import java.time.LocalDateTime;
import java.util.Map;
/**
 *
 * @author crist
 */
public class RespuestaError {
    private LocalDateTime fecha;
    private int estado;
    private String error;
    private String mensaje;
    private Map<String, String> validaciones;

    public RespuestaError(
            LocalDateTime fecha,
            int estado,
            String error,
            String mensaje,
            Map<String, String> validaciones) {

        this.fecha = fecha;
        this.estado = estado;
        this.error = error;
        this.mensaje = mensaje;
        this.validaciones = validaciones;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public int getEstado() {
        return estado;
    }

    public String getError() {
        return error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Map<String, String> getValidaciones() {
        return validaciones;
    }
}
