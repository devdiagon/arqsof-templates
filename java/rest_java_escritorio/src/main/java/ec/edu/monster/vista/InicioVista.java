package ec.edu.monster.vista;

import ec.edu.monster.vista.estilo.EstilosUi;
import ec.edu.monster.vista.estilo.Tema;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * DOMINIO (ejemplo): pantalla a la que se llega tras el login. Muestra el patrón de toda pantalla protegida:
 * encabezado, contenido y un botón secundario de salida. Se reemplaza por las pantallas del dominio
 * (ver README §4 y docs/CLIENT_STYLES_JAVA.md §5).
 */
public class InicioVista extends JPanel {

    private final JButton cerrarSesion = EstilosUi.botonSecundario("Cerrar sesión");

    public InicioVista() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(izquierda(EstilosUi.encabezadoPantalla("Inicio")));
        add(Box.createVerticalStrut(Tema.ESPACIO_ACCIONES_SALIDA));
        cerrarSesion.setMaximumSize(new Dimension(Integer.MAX_VALUE, cerrarSesion.getPreferredSize().height));
        add(izquierda(cerrarSesion));
    }

    public JButton getBotonCerrarSesion() {
        return cerrarSesion;
    }

    public void alCerrarSesion(Runnable accion) {
        cerrarSesion.addActionListener(e -> accion.run());
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
