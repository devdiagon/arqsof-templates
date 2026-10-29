package ec.edu.monster.vista;

/**
 * Textos de pantalla. Los mismos valores en todos los clientes del proyecto (los define su docs/PROJECT.md);
 * aquí solo van los genéricos. Los del dominio (títulos, etiquetas, mensajes de validación) se agregan en cada proyecto.
 * El usuario ve siempre mensajes, nunca códigos.
 */
public final class Textos {

    /** DOMINIO: nombre de la aplicación: título de la ventana y de la pantalla de acceso. */
    public static final String NOMBRE_APLICACION = "Cliente REST";

    public static final String INICIO = "Inicio";
    public static final String CANCELAR = "Cancelar";
    public static final String PROCESANDO = "Procesando…";
    public static final String CONSULTANDO = "Consultando…";
    public static final String INICIANDO_SESION = "Iniciando sesión…";
    public static final String ERROR_INESPERADO = "Ocurrió un error inesperado, intente nuevamente";
    public static final String SESION_NO_VALIDA = "Su sesión ya no es válida, inicie sesión nuevamente";

    /** Pantalla de acceso, para los proyectos que definen autenticación (LoginVista). */
    public static final String USUARIO = "Usuario";
    public static final String CLAVE = "Clave";
    public static final String INICIAR_SESION = "Iniciar sesión";
    public static final String CERRAR_SESION = "Cerrar sesión";

    private static final String CAMPO_OBLIGATORIO = "El campo %s es obligatorio";

    private Textos() {
    }

    /** {@code El campo {campo} es obligatorio}, con la etiqueta de pantalla en minúscula. */
    public static String campoObligatorio(String etiqueta) {
        return String.format(CAMPO_OBLIGATORIO, etiqueta.toLowerCase());
    }
}
