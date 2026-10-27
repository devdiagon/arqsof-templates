package ec.edu.monster.vista;

import java.util.Locale;

/**
 * Textos de pantalla. Los mismos valores en todos los clientes del proyecto (los define su docs/PROJECT.md);
 * aquí solo van los genéricos. Los del dominio (títulos, etiquetas, mensajes de validación) se agregan en cada proyecto.
 * El usuario ve siempre mensajes, nunca códigos. Los textos viven aquí y no en {@code strings.xml} (salvo
 * {@code app_name}, que Android exige para el manifiesto): así la regla de copia y la comparación de valores entre clientes se mantienen.
 */
public final class Textos {

    /** DOMINIO: nombre de la aplicación: título de la pantalla de acceso. Debe coincidir con {@code app_name} de strings.xml. */
    public static final String NOMBRE_APLICACION = "Cliente SOAP";

    public static final String INICIO = "Inicio";
    public static final String MENSAJE_INICIO = "Plantilla base sin dominio. Reemplace esta pantalla por las del proyecto (ver README.md).";
    public static final String CANCELAR = "Cancelar";
    public static final String PROCESANDO = "Procesando…";
    public static final String CONSULTANDO = "Consultando…";
    public static final String INICIANDO_SESION = "Iniciando sesión…";
    public static final String ERROR_INESPERADO = "Ocurrió un error inesperado, intente nuevamente";
    public static final String SESION_NO_VALIDA = "Su sesión ya no es válida, inicie sesión nuevamente";

    /** Pantalla de acceso, para los proyectos que definen autenticación (LoginActivity). */
    public static final String TITULO_LOGIN = "Inicio de sesión";
    public static final String USUARIO = "Usuario";
    public static final String CLAVE = "Clave";
    public static final String INICIAR_SESION = "Iniciar sesión";
    public static final String CERRAR_SESION = "Cerrar sesión";

    private static final String CAMPO_OBLIGATORIO = "El campo %s es obligatorio";

    private Textos() {
    }

    /** {@code El campo {campo} es obligatorio}, con la etiqueta de pantalla en minúscula. */
    public static String campoObligatorio(String etiqueta) {
        return String.format(CAMPO_OBLIGATORIO, etiqueta.toLowerCase(Locale.ROOT));
    }
}
