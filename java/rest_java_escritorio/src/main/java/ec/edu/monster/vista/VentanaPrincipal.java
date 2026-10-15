package ec.edu.monster.vista;

import ec.edu.monster.vista.estilo.PanelImagen;
import ec.edu.monster.vista.estilo.Tema;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Ventana única de la aplicación. A la izquierda muestra una pantalla a la vez (CardLayout), centrada
 * en una columna; a la derecha, la imagen decorativa. No contiene lógica: los controladores piden
 * qué pantalla mostrar.
 */
public class VentanaPrincipal extends JFrame {

    private static final String IMAGEN_LATERAL = "/ec/edu/monster/estaticos/fondo_app.jpeg";

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenedor = new JPanel(cartas);

    public VentanaPrincipal() {
        super(Textos.NOMBRE_APLICACION);
        contenedor.setBackground(Tema.FONDO);
        contenedor.setPreferredSize(new Dimension(Tema.ANCHO_PANEL_FORMULARIO, Tema.VENTANA_ALTO));

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);
        raiz.add(contenedor, BorderLayout.WEST);
        raiz.add(new PanelImagen(IMAGEN_LATERAL), BorderLayout.CENTER);
        setContentPane(raiz);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(Tema.VENTANA_ANCHO, Tema.VENTANA_ALTO));
        setSize(Tema.VENTANA_ANCHO_INICIAL, Tema.VENTANA_ALTO_INICIAL);
        setLocationRelativeTo(null);
    }

    public void agregar(Pantalla pantalla, JPanel panel) {
        JPanel envoltura = new JPanel(new GridBagLayout());
        envoltura.setBackground(Tema.FONDO);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(Tema.MARGEN_PANTALLA, Tema.MARGEN_PANTALLA, Tema.MARGEN_PANTALLA, Tema.MARGEN_PANTALLA);
        envoltura.add(panel, c);
        contenedor.add(envoltura, pantalla.name());
    }

    public void mostrar(Pantalla pantalla) {
        cartas.show(contenedor, pantalla.name());
    }
}
