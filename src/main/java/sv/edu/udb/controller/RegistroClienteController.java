package sv.edu.udb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.dto.RegistroClienteDTO;
import sv.edu.udb.dto.RegistroClienteRespuestaDTO;
import sv.edu.udb.dto.RespuestaError;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.service.RegistroClienteService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro público de clientes")
public class RegistroClienteController {

    private final RegistroClienteService registroClienteService;

    @PostMapping("/registro")
    @SecurityRequirements
    @Operation(
            summary = "Registrar una cuenta de cliente",
            description = "Endpoint público. Crea un usuario con rol CLIENTE (asignado "
                    + "siempre por el servidor) y lo vincula a un cliente. Si ya existe "
                    + "un cliente sin cuenta con el mismo correo, se vincula a ese cliente. "
                    + "No devuelve token: después hay que iniciar sesión."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos, formato inválido, "
                    + "contraseña que no cumple la política o contraseñas que no coinciden",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "409", description = "El correo ya pertenece a otra cuenta",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<RegistroClienteRespuestaDTO> registrar(
            @Valid @RequestBody RegistroClienteDTO datos) {

        Cliente cliente = registroClienteService.registrar(datos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegistroClienteRespuestaDTO(
                        cliente.getUsuario().getId(),
                        cliente.getIdCliente(),
                        cliente.getUsuario().getNombre(),
                        cliente.getUsuario().getEmail(),
                        cliente.getUsuario().getRol().getNombre(),
                        "Cuenta creada, ya puedes iniciar sesión"
                ));
    }
}
