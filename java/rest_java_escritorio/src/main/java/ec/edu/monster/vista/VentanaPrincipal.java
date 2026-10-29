package ec.edu.monster.vista;

import ec.edu.monster.vista.estilo.PanelImagen;
import ec.edu.monster.vista.estilo.Tema;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * Ventana única de la aplicación, con dos modos (docs/PANTALLAS.md §1):
 * <ul>
 *   <li><b>Acceso (dividido):</b> la pantalla en una columna centrada a la izquierda y la imagen a la derecha.</li>
 *   <li><b>Aplicación (ancho completo):</b> la imagen es el fondo de la ventana, centrada y con opacidad, y la pantalla
 *       se centra encima; si no cabe en alto, se desplaza.</li>
 * </ul>
 * No contiene lógica: quien arma las pantallas pide cuál mostrar.
 */
public class VentanaPrincipal extends JFrame {

    private static final String IMAGEN = "/ec/edu/monster/estaticos/fondo_app.png";
    private static final String MODO_ACCESO = "ACCESO";
    private static final String MODO_APLICACION = "APLICACION";

    private final CardLayout modos = new CardLayout();
    private final JPanel raiz = new JPanel(modos);

    private final CardLayout cartasAcceso = new CardLayout();
    private final JPanel contenedorAcceso = new JPanel(cartasAcceso);

    private final CardLayout cartasAplicacion = new CardLayout();
    private final PanelImagen fondoAplicacion = new PanelImagen(IMAGEN, Tema.OPACIDAD_IMAGEN);

    private final Map<Pantalla, JPanel> vistas = new EnumMap<>(Pantalla.class);

    public VentanaPrincipal() {
        super(Textos.NOMBRE_APLICACION);

        contenedorAcceso.setBackground(Tema.FONDO);
        contenedorAcceso.setPreferredSize(new Dimension(Tema.ANCHO_PANEL_FORMULARIO, Tema.VENTANA_ALTO));
        JPanel acceso = new JPanel(new BorderLayout());
        acceso.setBackground(Tema.FONDO);
        acceso.add(contenedorAcceso, BorderLayout.WEST);
        acceso.add(new PanelImagen(IMAGEN), BorderLayout.CENTER);

        fondoAplicacion.setLayout(cartasAplicacion);

        raiz.add(acceso, MODO_ACCESO);
        raiz.add(fondoAplicacion, MODO_APLICACION);
        setContentPane(raiz);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(Tema.VENTANA_ANCHO, Tema.VENTANA_ALTO));
        setSize(Tema.VENTANA_ANCHO_INICIAL, Tema.VENTANA_ALTO_INICIAL);
        setLocationRelativeTo(null);
    }

    public void agregar(Pantalla pantalla, JPanel vista) {
        vistas.put(pantalla, vista);
        if (pantalla.esAcceso()) {
            contenedorAcceso.add(centrada(vista), pantalla.name());
        } else {
            JScrollPane desplazamiento = new JScrollPane(contenedorPantalla(vista, pantalla.esAnchoCompleto()));
            desplazamiento.setOpaque(false);
            desplazamiento.getViewport().setOpaque(false);
            desplazamiento.setBorder(null);
            desplazamiento.getVerticalScrollBar().setUnitIncrement(16);
            fondoAplicacion.add(desplazamiento, pantalla.name());
        }
    }

    /** Muestra la pantalla (y su modo) y le pide que se deje limpia. */
    public void mostrar(Pantalla pantalla) {
        if (pantalla.esAcceso()) {
            modos.show(raiz, MODO_ACCESO);
            cartasAcceso.show(contenedorAcceso, pantalla.name());
        } else {
            modos.show(raiz, MODO_APLICACION);
            cartasAplicacion.show(fondoAplicacion, pantalla.name());
        }
        if (vistas.get(pantalla) instanceof VistaBase vista) {
            vista.alMostrar();
        }
    }

    // ---- Disposición ----

    private static JPanel centrada(JPanel vista) {
        JPanel envoltura = new JPanel(new GridBagLayout());
        envoltura.setBackground(Tema.FONDO);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = margen();
        envoltura.add(vista, c);
        return envoltura;
    }

    private static ContenedorPantalla contenedorPantalla(JPanel vista, boolean anchoCompleto) {
        ContenedorPantalla envoltura = new ContenedorPantalla();
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.insets = margen();
        if (anchoCompleto) {
            c.anchor = GridBagConstraints.NORTH;
            c.fill = GridBagConstraints.HORIZONTAL;
        } else {
            c.anchor = GridBagConstraints.CENTER;
        }
        envoltura.add(vista, c);
        return envoltura;
    }

    private static Insets margen() {
        return new Insets(Tema.MARGEN_PANTALLA, Tema.MARGEN_PANTALLA, Tema.MARGEN_PANTALLA, Tema.MARGEN_PANTALLA);
    }

    /**
     * Contenedor transparente de una pantalla: sigue el ancho de la ventana y, en alto, se estira para centrar el
     * contenido mientras cabe; si no cabe, deja de seguir el alto y aparece el desplazamiento.
     */
    private static final class ContenedorPantalla extends JPanel implements Scrollable {

        ContenedorPantalla() {
            super(new GridBagLayout());
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return orientacion == SwingConstants.VERTICAL ? visible.height : visible.width;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport vp && vp.getHeight() >= getPreferredSize().height;
        }
    }
}
