package ec.edu.monster.vista;

import ec.edu.monster.vista.estilo.EstilosUi;
import ec.edu.monster.vista.estilo.Tema;
import ec.edu.monster.vista.estilo.ZonaEstado;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/**
 * Base de las pantallas: panel pasivo (sin lógica) con la zona de estado, el estado «ocupado» que deshabilita los
 * controles y los auxiliares de disposición. Las vistas solo crean componentes con {@code EstilosUi}.
 */
public abstract class VistaBase extends JPanel {

    private final List<JComponent> controles = new ArrayList<>();
    private final int ancho;
    protected final ZonaEstado estado;
    protected final JButton cancelar = EstilosUi.botonSecundario(Textos.CANCELAR);

    /**
     * @param ancho ancho de la columna de contenido; 0 = el que dé el contenedor (ancho completo)
     * @param fondoPaneles fondo de los paneles de mensaje de la pantalla
     */
    protected VistaBase(int ancho, Color fondoPaneles) {
        this.ancho = ancho;
        this.estado = new ZonaEstado(fondoPaneles);
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    // ---- Ciclo de vida ----

    /** Se llama cada vez que la ventana muestra la pantalla: debe dejarla limpia y con el foco en su primer control. */
    public void alMostrar() {
    }

    /** Lo que había de una acción anterior (resultado, tarjetas) se oculta al empezar otra. */
    protected void ocultarResultado() {
    }

    // ---- Estado ----

    /** Mientras hay una llamada en curso: todos los controles deshabilitados y el indicador visible. */
    public void setOcupado(boolean ocupado, String texto) {
        for (JComponent control : controles) {
            control.setEnabled(!ocupado);
        }
        if (ocupado) {
            ocultarResultado();
            estado.mostrarOcupado(texto);
        } else {
            estado.ocultar();   // el resultado o el error se muestran después de liberar la pantalla
        }
    }

    public void mostrarError(String texto) {
        estado.mostrarError(texto);
    }

    public void mostrarExito(String texto) {
        estado.mostrarExito(texto);
    }

    public String getTextoEstado() {
        return estado.getTexto();
    }

    /** Registra los controles que se deshabilitan mientras la pantalla está ocupada. */
    protected void registrar(JComponent... nuevos) {
        controles.addAll(List.of(nuevos));
    }

    /** Quita la marca de error de todos los campos de texto registrados. */
    public void limpiarMarcas() {
        for (JComponent control : controles) {
            if (control instanceof JTextComponent) {
                EstilosUi.marcarError(control, false);
            }
        }
    }

    // ---- Eventos ----

    /** {@code Cancelar} y la tecla Esc. */
    public void alCancelar(Runnable accion) {
        cancelar.addActionListener(e -> accion.run());
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "cancelar");
        getActionMap().put("cancelar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cancelar.isShowing() && cancelar.isEnabled()) {
                    cancelar.doClick();
                }
            }
        });
    }

    // ---- Tamaño ----

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return ancho > 0 ? new Dimension(ancho, d.height) : d;
    }

    // ---- Auxiliares de disposición ----

    protected static <T extends JComponent> T izquierda(T componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        return componente;
    }

    protected static Component separacion(int alto) {
        return Box.createVerticalStrut(alto);
    }

    /** Fila de botones ajustados a su texto, centrados y separados 16. */
    protected static JPanel filaBotones(JButton... botones) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, Tema.ESPACIO_BLOQUE, 0));
        fila.setOpaque(false);
        for (JButton boton : botones) {
            fila.add(boton);
        }
        return izquierda(fila);
    }

    /** Centra un componente de ancho propio. */
    protected static JPanel centrado(JComponent componente) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        fila.setOpaque(false);
        fila.add(componente);
        return izquierda(fila);
    }

    /** Hace que el componente (un botón) ocupe todo el ancho de su fila. */
    protected static JPanel anchoCompleto(JComponent componente) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(componente, BorderLayout.CENTER);
        return izquierda(fila);
    }

    /** Un campo de texto con su etiqueta encima, de ancho máximo indicado. */
    protected static JPanel campoConEtiqueta(String etiqueta, JComponent campo, int anchoMaximo) {
        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.add(izquierda(EstilosUi.etiqueta(etiqueta)));
        columna.add(izquierda(campo));
        if (anchoMaximo > 0) {
            Dimension d = columna.getPreferredSize();
            columna.setPreferredSize(new Dimension(anchoMaximo, d.height));
            columna.setMaximumSize(new Dimension(anchoMaximo, d.height));
        }
        return izquierda(columna);
    }
}
