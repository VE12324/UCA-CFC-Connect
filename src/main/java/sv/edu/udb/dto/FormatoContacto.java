package sv.edu.udb.dto;


import java.util.regex.Pattern;

public final class FormatoContacto {


    /** DUI 00000000-0 o NIT 0000-000000-000-0. */
    public static final String DUI_NIT =
            "\\d{8}-\\d|\\d{4}-\\d{6}-\\d{3}-\\d";


    public static final String MENSAJE_DUI_NIT =
            "El DUI/NIT debe tener el formato 00000000-0 (DUI) "
                    + "o 0000-000000-000-0 (NIT)";


    /** Teléfono de El Salvador: 8 dígitos (inicia en 2, 6 o 7), con +503 opcional. */
    public static final String TELEFONO =
            "(\\+503 ?)?[267]\\d{3}-\\d{4}";


    public static final String MENSAJE_TELEFONO =
            "El teléfono debe tener 8 dígitos con el formato 0000-0000, "
                    + "iniciar con 2, 6 o 7 y puede llevar el prefijo +503";


    private static final Pattern PATRON_DUI_NIT = Pattern.compile(DUI_NIT);
    private static final Pattern PATRON_TELEFONO = Pattern.compile(TELEFONO);


    private FormatoContacto() {
    }


    public static boolean duiNitValido(String valor) {
        return valor != null && PATRON_DUI_NIT.matcher(valor.trim()).matches();
    }


    public static boolean telefonoValido(String valor) {
        return valor != null && PATRON_TELEFONO.matcher(valor.trim()).matches();
    }


    /** "+5037000-0000" → "+503 7000-0000"; sin prefijo queda igual. */
    public static String normalizarTelefono(String valor) {
        String telefono = valor.trim();
        return telefono.startsWith("+503") && !telefono.startsWith("+503 ")
                ? "+503 " + telefono.substring(4)
                : telefono;
    }
}
