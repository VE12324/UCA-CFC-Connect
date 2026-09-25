package sv.edu.udb.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import sv.edu.udb.service.UsuarioDetalleService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

       private final UsuarioDetalleService usuarioDetalleService;

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        usuarioDetalleService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {

        http
                .authenticationProvider(
                        authenticationProvider()
                )

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/**"
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/login",
                                "/error",
                                "/css/**",
                                "/iconos/**",
                                "/js/**",
                                "/images/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()

                        /*
                         * Paneles principales
                         */
                        .requestMatchers(
                                "/admin/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/recepcionista/**"
                        )
                        .hasRole("RECEPCIONISTA")

                        .requestMatchers(
                                "/contabilidad/**"
                        )
                        .hasRole("CONTABILIDAD")

                        /*
                         * Portal del cliente
                         */
                        .requestMatchers(
                                "/mi-cuenta/**"
                        )
                        .hasRole("CLIENTE")

                        /*
                         * Configuración exclusiva del administrador
                         */
                        .requestMatchers(
                                "/cursos/**",
                                "/diplomados/**",
                                "/espacios/**",
                                "/catering/**",
                                "/categorias/**",
                                "/modalidades/**",
                                "/docentes/**",
                                "/usuarios/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/api/usuarios/**"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Módulos administrados por recepción
                         */
                        .requestMatchers(
                                "/clientes/**",
                                "/participantes/**",
                                "/inscripciones/**",
                                "/cotizaciones/**",
                                "/alquileres/**",
                                "/solicitudes-catering/**",
                                "/agenda/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * Aprobar y rechazar cotizaciones:
                         * solamente el administrador
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/cotizaciones/*/aprobar",
                                "/api/cotizaciones/*/rechazar"
                        )
                        .hasRole("ADMIN")

                        /*
                         * API de cotizaciones
                         */
                        .requestMatchers(
                                "/api/cotizaciones/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * API de los módulos de recepción
                         */
                        .requestMatchers(
                                "/api/clientes/**",
                                "/api/participantes/**",
                                "/api/inscripciones/**",
                                "/api/alquileres/**",
                                "/api/solicitudes-catering/**",
                                "/api/agenda/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * Pagos y contabilidad
                         */
                        .requestMatchers(
                                "/api/pagos/**"
                        )
                        .hasRole("CONTABILIDAD")

                        /*
                         * Consulta de catálogos
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/cursos/**",
                                "/api/diplomados/**",
                                "/api/espacios/**",
                                "/api/catering/**",
                                "/api/categorias/**",
                                "/api/modalidades/**",
                                "/api/docentes/**"
                        )
                        .authenticated()

                        /*
                         * Modificación de catálogos
                         */
                        .requestMatchers(
                                "/api/cursos/**",
                                "/api/diplomados/**",
                                "/api/espacios/**",
                                "/api/catering/**",
                                "/api/categorias/**",
                                "/api/modalidades/**",
                                "/api/docentes/**"
                        )
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form
                        .loginPage(
                                "/login"
                        )
                        .loginProcessingUrl(
                                "/login"
                        )
                        .successHandler(
                                manejadorExito()
                        )
                        .failureUrl(
                                "/login?error"
                        )
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl(
                                "/logout"
                        )
                        .logoutSuccessUrl(
                                "/login?logout"
                        )
                        .invalidateHttpSession(
                                true
                        )
                        .clearAuthentication(
                                true
                        )
                        .deleteCookies(
                                "JSESSIONID"
                        )
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler manejadorExito() {

        return (request, response, authentication) -> {

            String rol = authentication
                    .getAuthorities()
                    .stream()
                    .findFirst()
                    .map(
                            GrantedAuthority::getAuthority
                    )
                    .orElse(
                            "ROLE_CLIENTE"
                    );

            String destino = switch (rol) {

                case "ROLE_ADMIN" ->
                    "/admin/dashboard";

                case "ROLE_RECEPCIONISTA" ->
                    "/recepcionista/dashboard";

                case "ROLE_CONTABILIDAD" ->
                    "/contabilidad/dashboard";

                case "ROLE_CLIENTE" ->
                    "/mi-cuenta";

                default ->
                    "/login?sin-acceso";
            };

            response.sendRedirect(
                    request.getContextPath()
                    + destino
            );
        };
    }
}
