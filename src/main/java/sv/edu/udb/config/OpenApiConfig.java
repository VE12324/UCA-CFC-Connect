package sv.edu.udb.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Documentación OpenAPI / Swagger UI.
 */
@Configuration
public class OpenApiConfig {


    public static final String ESQUEMA_SEGURIDAD = "bearerAuth";


    @Bean
    public OpenAPI documentacionApi() {


        return new OpenAPI()
                .info(new Info()
                        .title("UCA-CFC Connect - API REST")
                        .version("1.0")
                        .description("API del sistema de gestión del Centro de Formación "
                                + "Continua (UCA). Para probar los endpoints protegidos: "
                                + "1) POST /api/auth/login con correo y contraseña, "
                                + "2) copiar el campo token, 3) botón Authorize y pegarlo."))
                .components(new Components()
                        .addSecuritySchemes(
                                ESQUEMA_SEGURIDAD,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token obtenido en POST /api/auth/login")
                        ))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD));
    }
}
