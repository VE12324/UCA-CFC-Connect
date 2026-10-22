package sv.edu.udb.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import sv.edu.udb.security.JwtAuthenticationFilter;
import sv.edu.udb.security.JwtService;
import sv.edu.udb.security.RespuestaErrorApi;
import sv.edu.udb.security.TokenRevocadoService;
import sv.edu.udb.service.UsuarioDetalleService;


/**
 * Seguridad con dos cadenas de filtros:
 *
 * 1. API REST (/api/**): stateless con JWT. Sin sesión ni CSRF; el token se
 *    envía en "Authorization: Bearer <token>". 401/403 en JSON.
 * 2. Interfaz web (Thymeleaf, todo lo demás): login por formulario con
 *    sesión, igual que antes.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final UsuarioDetalleService usuarioDetalleService;
    private final RequestCache cachePeticiones = new HttpSessionRequestCache();
    private final JwtService jwtService;
    private final TokenRevocadoService tokenRevocadoService;
    private final RespuestaErrorApi respuestaErrorApi;


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


    /**
     * Cadena 1: API REST stateless con JWT.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(
            HttpSecurity http) throws Exception {


        http
                .securityMatcher(
                        "/api/**"
                )


                .authenticationProvider(
                        authenticationProvider()
                )


                .csrf(AbstractHttpConfigurer::disable)


                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)


                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(
                                respuestaErrorApi
                        )
                        .accessDeniedHandler(
                                respuestaErrorApi
                        )
                )


                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                jwtService,
                                tokenRevocadoService,
                                usuarioDetalleService
                        ),
                        UsernamePasswordAuthenticationFilter.class
                )


                .authorizeHttpRequests(auth -> auth


                        /*
                         * Autenticación. cambiar-password queda abierto
                         * porque acepta dos modos: con JWT (contraseña
                         * actual) o sin JWT (token de recuperación); el
                         * controlador exige uno de los dos.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/recuperar-password",
                                "/api/auth/registro"
                        )
                        .permitAll()


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/auth/cambiar-password"
                        )
                        .permitAll()


                        .requestMatchers(
                                "/api/usuarios/**"
                        )
                        .hasRole("ADMIN")


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
                         * API de los módulos de recepción.
                         * La agenda se expone en /api/actividades.
                         */
                        .requestMatchers(
                                "/api/clientes/**",
                                "/api/participantes/**",
                                "/api/inscripciones/**",
                                "/api/alquileres/**",
                                "/api/solicitudes-catering/**",
                                "/api/actividades/**"
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
                );


        return http.build();
    }


    /**
     * Cadena 2: interfaz web con login por formulario y sesión.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {


        http
                .authenticationProvider(
                        authenticationProvider()
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


                        /*
                         * Auto-registro público de clientes
                         */
                        .requestMatchers(
                                "/registro"
                        )
                        .permitAll()


                        /*
                         * Sitio público (solo lectura). "/" muestra la
                         * portada a anónimos y clientes; el personal sigue
                         * yendo a su panel (DashboardController.raiz).
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/",
                                "/portal/inicio",
                                "/portal/oferta",
                                "/portal/oferta/curso/*",
                                "/portal/oferta/diplomado/*",
                                "/portal/espacios",
                                "/portal/catering"
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
                        "/";


                default ->
                        "/login?sin-acceso";
            };


            /*
             * Un CLIENTE que llegó al login desde "Reservar" o "Solicitar"
             * regresa a la página que pedía (petición guardada por Spring
             * Security). Solo se aceptan rutas del sitio y del portal.
             */
            if ("ROLE_CLIENTE".equals(rol)) {


                String guardada = rutaGuardadaDelPortal(request, response);


                if (guardada != null) {
                    response.sendRedirect(guardada);
                    return;
                }
            }


            response.sendRedirect(
                    request.getContextPath()
                            + destino
            );
        };
    }


    private String rutaGuardadaDelPortal(
            HttpServletRequest request,
            HttpServletResponse response) {


        SavedRequest guardada = cachePeticiones.getRequest(request, response);


        if (guardada == null) {
            return null;
        }


        cachePeticiones.removeRequest(request, response);


        String url = guardada.getRedirectUrl();
        String ruta = URI.create(url).getPath();
        String contexto = request.getContextPath();


        if (ruta == null || !ruta.startsWith(contexto)) {
            return null;
        }


        ruta = ruta.substring(contexto.length());


        boolean delPortal = ruta.equals("/")
                || ruta.startsWith("/portal/")
                || ruta.equals("/mi-cuenta")
                || ruta.startsWith("/mi-cuenta/");


        return delPortal ? url : null;
    }
}


