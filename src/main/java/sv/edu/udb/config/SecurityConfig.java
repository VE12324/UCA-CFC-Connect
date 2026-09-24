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
        // Spring Security 6.4+: UserDetailsService va en el constructor, no en setter
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetalleService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/css/**", "/iconos/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // Paneles y módulos propios de cada rol
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/recepcionista/**").hasRole("RECEPCIONISTA")
                .requestMatchers("/contabilidad/**", "/api/pagos/**").hasRole("CONTABILIDAD")

                // Módulos de configuración académica/institucional: exclusivos de Administrador
                .requestMatchers("/cursos/**", "/diplomados/**", "/espacios/**", "/catering/**",
                        "/categorias/**", "/modalidades/**", "/docentes/**", "/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                // API de catálogos: cualquier usuario autenticado puede consultarlos (GET),
                // pero solo Administrador puede crear, modificar o eliminar
                .requestMatchers(HttpMethod.GET, "/api/cursos/**", "/api/diplomados/**", "/api/espacios/**",
                        "/api/catering/**", "/api/categorias/**", "/api/modalidades/**", "/api/docentes/**").authenticated()
                .requestMatchers("/api/cursos/**", "/api/diplomados/**", "/api/espacios/**",
                        "/api/catering/**", "/api/categorias/**", "/api/modalidades/**", "/api/docentes/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(manejadorExito())
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        // Sin accessDeniedPage: un acceso sin permisos responde 403 y Spring Boot
        // renderiza templates/error/403.html
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler manejadorExito() {
        return (request, response, authentication) -> {
            String rol = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_CLIENTE");

            String destino = switch (rol) {
                case "ROLE_ADMIN" -> "/admin/dashboard";
                case "ROLE_RECEPCIONISTA" -> "/recepcionista/dashboard";
                case "ROLE_CONTABILIDAD" -> "/contabilidad/dashboard";
                default -> "/login?sin-acceso";
            };

            response.sendRedirect(request.getContextPath() + destino);
        };
    }
}
