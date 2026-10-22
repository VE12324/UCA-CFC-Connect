package sv.edu.udb.security;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("JwtService - generación y validación de tokens JWT")
class JwtServiceTest {


    private static final String SECRETO =
            "clave-de-pruebas-unitarias-con-mas-de-32-caracteres";
    private static final long UNA_HORA_MS = 3_600_000L;


    private JwtService jwtService;


    @BeforeEach
    void prepararServicio() {
        jwtService = new JwtService(SECRETO, UNA_HORA_MS);
    }


    @Test
    @DisplayName("Genera un token con correo, rol, identificador y expiración")
    void generarTokenIncluyeDatosDelUsuario() {
        // Act
        String token = jwtService.generarToken("admin@uca.edu.sv", "ADMIN");


        // Assert
        Claims claims = jwtService.validarYObtenerClaims(token);
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(claims.getSubject()).isEqualTo("admin@uca.edu.sv");
        assertThat(claims.get(JwtService.CLAIM_ROL, String.class)).isEqualTo("ADMIN");
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(UNA_HORA_MS);
    }


    @Test
    @DisplayName("Cada token tiene un identificador distinto (permite revocarlos por separado)")
    void cadaTokenTieneIdentificadorUnico() {
        // Act
        String primero = jwtService.generarToken("admin@uca.edu.sv", "ADMIN");
        String segundo = jwtService.generarToken("admin@uca.edu.sv", "ADMIN");


        // Assert
        assertThat(jwtService.validarYObtenerClaims(primero).getId())
                .isNotEqualTo(jwtService.validarYObtenerClaims(segundo).getId());
    }


    @Test
    @DisplayName("Un token recién generado es válido y se extrae su correo")
    void tokenGeneradoEsValido() {
        // Arrange
        String token = jwtService.generarToken("recepcion@uca.edu.sv", "RECEPCIONISTA");


        // Act + Assert
        assertThat(jwtService.esTokenValido(token)).isTrue();
        assertThat(jwtService.extraerCorreo(token)).isEqualTo("recepcion@uca.edu.sv");
    }


    @Test
    @DisplayName("Un token expirado no es válido")
    void tokenExpiradoNoEsValido() {
        // Arrange: expiración negativa = el token nace vencido
        JwtService servicioVencido = new JwtService(SECRETO, -1_000L);
        String token = servicioVencido.generarToken("admin@uca.edu.sv", "ADMIN");


        // Act + Assert
        assertThat(jwtService.esTokenValido(token)).isFalse();
        assertThatThrownBy(() -> jwtService.validarYObtenerClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }


    @Test
    @DisplayName("Un token con el contenido manipulado (rol cambiado) no es válido")
    void tokenConPayloadManipuladoNoEsValido() {
        // Arrange: se reemplaza el payload por uno con rol ADMIN sin volver a firmar
        String token = jwtService.generarToken("cliente@correo.com", "CLIENTE");
        String[] partes = token.split("\\.");
        String payloadFalso = Base64.getUrlEncoder().withoutPadding().encodeToString(
                new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8)
                        .replace("\"CLIENTE\"", "\"ADMIN\"")
                        .getBytes(StandardCharsets.UTF_8)
        );
        String tokenManipulado = partes[0] + "." + payloadFalso + "." + partes[2];


        // Act + Assert
        assertThat(jwtService.esTokenValido(tokenManipulado)).isFalse();
        assertThatThrownBy(() -> jwtService.validarYObtenerClaims(tokenManipulado))
                .isInstanceOf(SignatureException.class);
    }


    @Test
    @DisplayName("Un token con la firma alterada no es válido")
    void tokenConFirmaAlteradaNoEsValido() {
        // Arrange
        String token = jwtService.generarToken("admin@uca.edu.sv", "ADMIN");
        int posicion = token.lastIndexOf('.') + 5;
        char reemplazo = token.charAt(posicion) == 'A' ? 'B' : 'A';
        String tokenAlterado = token.substring(0, posicion) + reemplazo
                + token.substring(posicion + 1);


        // Act + Assert
        assertThat(jwtService.esTokenValido(tokenAlterado)).isFalse();
    }


    @Test
    @DisplayName("Un token firmado con otra clave no es válido")
    void tokenDeOtraClaveNoEsValido() {
        // Arrange
        JwtService otroServidor = new JwtService(
                "otra-clave-distinta-tambien-de-mas-de-32-caracteres", UNA_HORA_MS);
        String tokenAjeno = otroServidor.generarToken("admin@uca.edu.sv", "ADMIN");


        // Act + Assert
        assertThat(jwtService.esTokenValido(tokenAjeno)).isFalse();
    }


    @Test
    @DisplayName("Textos vacíos o mal formados no son tokens válidos")
    void textoInvalidoNoEsToken() {
        assertThat(jwtService.esTokenValido(null)).isFalse();
        assertThat(jwtService.esTokenValido("")).isFalse();
        assertThat(jwtService.esTokenValido("esto.no.es-un-jwt")).isFalse();
    }


    @Test
    @DisplayName("Rechaza una clave secreta de menos de 32 caracteres")
    void claveCortaSeRechaza() {
        assertThatThrownBy(() -> new JwtService("clave-corta", UNA_HORA_MS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}


