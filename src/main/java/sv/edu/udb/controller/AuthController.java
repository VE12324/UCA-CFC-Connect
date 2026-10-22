package sv.edu.udb.controller;


import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.dto.CambiarPasswordRequestDTO;
import sv.edu.udb.dto.LoginRequestDTO;
import sv.edu.udb.dto.LoginRespuestaDTO;
import sv.edu.udb.dto.MensajeRespuestaDTO;
import sv.edu.udb.dto.RecuperarPasswordRequestDTO;
import sv.edu.udb.dto.RespuestaError;
import sv.edu.udb.security.JwtAuthenticationFilter;
import sv.edu.udb.security.JwtService;
import sv.edu.udb.security.RecuperacionPasswordService;
import sv.edu.udb.security.TokenRevocadoService;
import sv.edu.udb.service.UsuarioService;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login con JWT, logout, recuperación y cambio de contraseña")
public class AuthController {


    private static final String PREFIJO_ROL = "ROLE_";


    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;
    private final TokenRevocadoService tokenRevocadoService;
    private final RecuperacionPasswordService recuperacionPasswordService;
    private final UsuarioService usuarioService;


    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Iniciar sesión y obtener un token JWT",
            description = "Valida correo y contraseña (BCrypt). Las cuentas con estado "
                    + "inactivo no pueden iniciar sesión. Devuelve el token que debe "
                    + "enviarse en el encabezado Authorization: Bearer <token>."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales correctas, token emitido"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos o con formato inválido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta deshabilitada",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDTO datos) {


        Authentication autenticacion;


        try {
            autenticacion = authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            datos.getCorreo().trim().toLowerCase(),
                            datos.getPassword()
                    )
            );


        } catch (DisabledException excepcion) {


            return noAutorizado("La cuenta está deshabilitada");


        } catch (AuthenticationException excepcion) {


            return noAutorizado("Correo o contraseña incorrectos");
        }


        String rol = autenticacion.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(nombre -> nombre.replaceFirst("^" + PREFIJO_ROL, ""))
                .orElse("");


        String correo = autenticacion.getName();


        return ResponseEntity.ok(
                new LoginRespuestaDTO(
                        jwtService.generarToken(correo, rol),
                        "Bearer",
                        jwtService.getExpiracionMs() / 1000,
                        correo,
                        rol
                )
        );
    }


    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión (revocar el token)",
            description = "Agrega el token actual a la lista de tokens revocados, que se "
                    + "guarda en memoria hasta que el token expire. Después de esto el "
                    + "token responde 401."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token revocado"),
            @ApiResponse(responseCode = "401", description = "Sin token o token inválido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public MensajeRespuestaDTO logout(HttpServletRequest request) {


        Claims claims = (Claims) request.getAttribute(
                JwtAuthenticationFilter.ATRIBUTO_CLAIMS
        );


        tokenRevocadoService.revocar(
                claims.getId(),
                claims.getExpiration().toInstant()
        );


        return new MensajeRespuestaDTO("Sesión cerrada. El token ya no es válido.");
    }


    @PostMapping("/recuperar-password")
    @SecurityRequirements
    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = "Si el correo pertenece a una cuenta activa, genera un token de "
                    + "recuperación de un solo uso válido por 15 minutos y lo escribe en la "
                    + "consola del servidor (simulación de envío de correo). La respuesta es "
                    + "la misma exista o no el correo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud recibida"),
            @ApiResponse(responseCode = "400", description = "Correo vacío o con formato inválido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public MensajeRespuestaDTO recuperarPassword(
            @Valid @RequestBody RecuperarPasswordRequestDTO datos) {


        recuperacionPasswordService.solicitar(datos.getCorreo());


        return new MensajeRespuestaDTO(
                "Si el correo está registrado, se enviaron las instrucciones "
                        + "para restablecer la contraseña."
        );
    }


    @PutMapping("/cambiar-password")
    @Operation(
            summary = "Cambiar la contraseña",
            description = "Dos modos: (A) con token JWT, enviando passwordActual y "
                    + "passwordNueva; (B) sin token JWT, enviando tokenRecuperacion "
                    + "(de /recuperar-password) y passwordNueva. La nueva contraseña debe "
                    + "tener mínimo 8 caracteres, una mayúscula, una minúscula, un número "
                    + "y un carácter especial (@$!%*?&)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta, token "
                    + "de recuperación inválido/expirado o nueva contraseña fuera de la política",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "Sin token JWT ni token de recuperación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<?> cambiarPassword(
            @Valid @RequestBody CambiarPasswordRequestDTO datos,
            Authentication autenticacion) {


        if (datos.getTokenRecuperacion() != null
                && !datos.getTokenRecuperacion().isBlank()) {


            String tokenRecuperacion = datos.getTokenRecuperacion().trim();


            String correo = recuperacionPasswordService.obtenerCorreo(
                    tokenRecuperacion
            );


            usuarioService.restablecerPassword(correo, datos.getPasswordNueva());


            recuperacionPasswordService.invalidar(tokenRecuperacion);


            return ResponseEntity.ok(
                    new MensajeRespuestaDTO("Contraseña restablecida correctamente.")
            );
        }


        if (autenticacion == null
                || autenticacion instanceof AnonymousAuthenticationToken
                || !autenticacion.isAuthenticated()) {


            return noAutorizado(
                    "Inicie sesión (token JWT) o envíe un token de recuperación"
            );
        }


        usuarioService.cambiarPassword(
                autenticacion.getName(),
                datos.getPasswordActual(),
                datos.getPasswordNueva()
        );


        return ResponseEntity.ok(
                new MensajeRespuestaDTO("Contraseña actualizada correctamente.")
        );
    }


    private ResponseEntity<RespuestaError> noAutorizado(String mensaje) {


        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new RespuestaError(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        "No autenticado",
                        mensaje,
                        null
                ));
    }
}




