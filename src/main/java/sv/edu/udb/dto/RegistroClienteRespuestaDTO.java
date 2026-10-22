package sv.edu.udb.dto;

public record RegistroClienteRespuestaDTO(
        Integer idUsuario,
        Integer idCliente,
        String nombre,
        String correo,
        String rol,
        String mensaje) {
}
