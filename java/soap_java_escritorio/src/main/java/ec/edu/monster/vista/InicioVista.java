package ec.edu.monster.vista;

import ec.edu.monster.vista.estilo.EstilosUi;
import ec.edu.monster.vista.estilo.Tema;

/**
 * DOMINIO (ejemplo): pantalla inicial de la plantilla (punto de enganche: el proyecto la reemplaza por su login o su menú).
 * Muestra el patrón de toda pantalla de la aplicación: una {@link VistaBase} pasiva, construida solo con {@code EstilosUi} y
 * {@code Tema}, con su encabezado y su contenido. Se reemplaza por las pantallas del dominio (README §4 y
 * docs/CLIENT_STYLES_JAVA.md §5).
 */
public class InicioVista extends VistaBase {

    public InicioVista() {
        super(Tema.ANCHO_CONTENIDO_FORMULARIO, Tema.PANEL_SOBRE_IMAGEN);
        add(izquierda(EstilosUi.encabezadoPantalla(Textos.INICIO)));
    }
}
