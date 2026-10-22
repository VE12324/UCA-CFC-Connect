package sv.edu.udb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Formulario "Mis datos" del portal del cliente. */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilClienteDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
    private String nombre;

    @NotBlank(message = "El DUI/NIT es obligatorio para completar tu registro")
    @Size(max = 20, message = "El DUI/NIT no puede superar los 20 caracteres")
    @Pattern(regexp = "(?:" + FormatoContacto.DUI_NIT + ")?", message = FormatoContacto.MENSAJE_DUI_NIT)
    private String dui;

    @Size(max = 150, message = "La empresa no puede superar los 150 caracteres")
    private String empresa;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio para completar tu registro")
    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    @Pattern(regexp = "(?:" + FormatoContacto.TELEFONO + ")?", message = FormatoContacto.MENSAJE_TELEFONO)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria para completar tu registro")
    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    private String direccion;

    private String passwordActual;
}
