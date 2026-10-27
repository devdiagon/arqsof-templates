package ec.edu.monster.vista;

import ec.edu.monster.vista.estilo.EstilosUi;
import ec.edu.monster.vista.estilo.Tema;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * DOMINIO (ejemplo): pantalla inicial de la plantilla (punto de enganche: el proyecto la reemplaza por su login o su menú).
 * Muestra el patrón de toda pantalla: encabezado y contenido, sin lógica. Se reemplaza por las pantallas del dominio
 * (ver README §4 y docs/CLIENT_STYLES_JAVA.md §5).
 */
public class InicioVista extends JPanel {

    public InicioVista() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(izquierda(EstilosUi.encabezadoPantalla(Textos.INICIO)));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Tema.ANCHO_COLUMNA, super.getPreferredSize().height);
    }

    private static <T extends JComponent> T izquierda(T componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        return componente;
    }
}
