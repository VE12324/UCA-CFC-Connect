package sv.edu.udb.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DefaultBindingErrorProcessor;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import sv.edu.udb.exception.ClienteNoVinculadoException;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.service.PerfilClienteService;

@ControllerAdvice(assignableTypes = {
        MiCuentaController.class,
        PaginaPortalCliente.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class PortalClienteAdvice {

    private final PerfilClienteService perfilClienteService;

    @InitBinder
    public void mensajesDeConversion(WebDataBinder binder) {

        binder.setBindingErrorProcessor(new DefaultBindingErrorProcessor() {

            @Override
            public void processPropertyAccessException(
                    PropertyAccessException excepcion,
                    BindingResult resultado) {

                Class<?> tipo = excepcion instanceof TypeMismatchException conversion
                        ? conversion.getRequiredType()
                        : null;

                String mensaje;

                if (tipo == LocalDate.class) {
                    mensaje = "La fecha no es válida";
                } else if (tipo == LocalTime.class) {
                    mensaje = "La hora no es válida";
                } else if (tipo != null && Number.class.isAssignableFrom(tipo)) {
                    mensaje = "Debe ser un número entero";
                } else {
                    mensaje = "El valor ingresado no es válido";
                }

                resultado.addError(new FieldError(
                        resultado.getObjectName(),
                        excepcion.getPropertyName(),
                        excepcion.getValue(),
                        true,
                        new String[] {"typeMismatch"},
                        null,
                        mensaje
                ));
            }
        });
    }

    @ModelAttribute
    public void cargarClienteDeSesion(
            Authentication authentication,
            HttpServletRequest request,
            Model modelo) {

        if (!esCliente(authentication)) {
            return;
        }

        Optional<Cliente> cliente =
                perfilClienteService.buscarCliente(authentication.getName());

        if (cliente.isEmpty()) {

            String ruta = request.getRequestURI()
                    .substring(request.getContextPath().length());

            if (ruta.startsWith("/mi-cuenta")) {
                throw new ClienteNoVinculadoException();
            }

            return;
        }

        modelo.addAttribute("clienteSesion", cliente.get());
        modelo.addAttribute(
                "perfilIncompleto",
                perfilClienteService.estaIncompleto(cliente.get())
        );
    }

    @ExceptionHandler(ClienteNoVinculadoException.class)
    public ModelAndView clienteNoVinculado(
            ClienteNoVinculadoException excepcion) {

        ModelAndView vista = new ModelAndView("portal/sin-vinculo");
        vista.addObject("mensaje", excepcion.getMessage());

        return vista;
    }

    @ExceptionHandler(RecursoNoEncontrado.class)
    public ModelAndView recursoNoEncontrado(
            RecursoNoEncontrado excepcion) {

        ModelAndView vista = new ModelAndView("portal/no-encontrado");
        vista.addObject("mensaje", excepcion.getMessage());
        vista.setStatus(HttpStatus.NOT_FOUND);

        return vista;
    }

    private boolean esCliente(Authentication authentication) {

        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(rol -> "ROLE_CLIENTE"
                        .equals(rol.getAuthority()));
    }
}
