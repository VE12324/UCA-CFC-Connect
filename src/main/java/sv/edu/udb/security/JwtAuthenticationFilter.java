package sv.edu.udb.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import sv.edu.udb.service.UsuarioDetalleService;


public class JwtAuthenticationFilter extends OncePerRequestFilter {


    public static final String ATRIBUTO_ERROR = "jwt.error";
    public static final String ATRIBUTO_CLAIMS = "jwt.claims";


    private static final String PREFIJO = "Bearer ";


    private final JwtService jwtService;
    private final TokenRevocadoService tokenRevocadoService;
    private final UsuarioDetalleService usuarioDetalleService;


    public JwtAuthenticationFilter(
            JwtService jwtService,
            TokenRevocadoService tokenRevocadoService,
            UsuarioDetalleService usuarioDetalleService) {


        this.jwtService = jwtService;
        this.tokenRevocadoService = tokenRevocadoService;
        this.usuarioDetalleService = usuarioDetalleService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {


        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);


        if (encabezado == null || !encabezado.startsWith(PREFIJO)) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = encabezado.substring(PREFIJO.length()).trim();


        try {
            Claims claims = jwtService.validarYObtenerClaims(token);


            if (tokenRevocadoService.estaRevocado(claims.getId())) {


                request.setAttribute(
                        ATRIBUTO_ERROR,
                        "El token fue revocado (se cerró la sesión)"
                );


            } else {


                UserDetails usuario = usuarioDetalleService
                        .loadUserByUsername(claims.getSubject());


                if (!usuario.isEnabled()) {


                    request.setAttribute(
                            ATRIBUTO_ERROR,
                            "La cuenta está deshabilitada"
                    );


                } else {


                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    usuario,
                                    null,
                                    usuario.getAuthorities()
                            );


                    autenticacion.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );


                    SecurityContextHolder.getContext()
                            .setAuthentication(autenticacion);


                    request.setAttribute(ATRIBUTO_CLAIMS, claims);
                }
            }


        } catch (JwtException | IllegalArgumentException excepcion) {


            request.setAttribute(
                    ATRIBUTO_ERROR,
                    "El token no es válido o ya expiró"
            );


        } catch (UsernameNotFoundException excepcion) {


            request.setAttribute(
                    ATRIBUTO_ERROR,
                    "La cuenta del token ya no existe"
            );
        }


        filterChain.doFilter(request, response);
    }
}

