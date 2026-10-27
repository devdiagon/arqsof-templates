package ec.edu.monster.vista;

/**
 * Pantallas que la ventana principal muestra. El acceso usa la disposición dividida (la pantalla a la izquierda y la imagen a
 * la derecha); las demás, el ancho completo sobre la imagen de fondo. {@code anchoCompleto}: el contenido ocupa todo el ancho
 * en lugar de una columna centrada (p. ej. una tabla).
 *
 * DOMINIO: agregar aquí una constante por pantalla nueva.
 */
public enum Pantalla {
    /** Pantalla de acceso, para los proyectos que definen autenticación (LoginVista). */
    LOGIN(true, false),
    INICIO(false, false);

    private final boolean acceso;
    private final boolean anchoCompleto;

    Pantalla(boolean acceso, boolean anchoCompleto) {
        this.acceso = acceso;
        this.anchoCompleto = anchoCompleto;
    }

    public boolean esAcceso() {
        return acceso;
    }

    public boolean esAnchoCompleto() {
        return anchoCompleto;
    }
}
