package ec.edu.monster.vista.estilo;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Zona de estado de una pantalla: un solo lugar que muestra, de una en una, el indicador de ocupado con su texto,
 * un mensaje de error o un mensaje de éxito. Oculta hasta que hay algo que decir.
 */
public class ZonaEstado extends JPanel {

    private final PanelMensaje mensaje;
    private final IndicadorProgreso progreso = new IndicadorProgreso();
    private final JLabel textoOcupado = new JLabel();
    private final JPanel filaOcupado = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));

    /** @param fondo fondo de los paneles de mensaje ({@code PANEL_SOBRE_IMAGEN} o, en el acceso, {@code SUPERFICIE}) */
    public ZonaEstado(Color fondo) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        mensaje = new PanelMensaje(fondo);
        textoOcupado.setFont(EstilosUi.fuente(Font.PLAIN, Tema.TAM_MENSAJE));
        textoOcupado.setForeground(Tema.TEXTO);
        filaOcupado.setOpaque(false);
        filaOcupado.add(progreso);
        filaOcupado.add(textoOcupado);
        filaOcupado.setVisible(false);
        filaOcupado.setAlignmentX(Component.LEFT_ALIGNMENT);
        mensaje.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(filaOcupado);
        add(mensaje);
    }

    public void mostrarOcupado(String texto) {
        mensaje.ocultar();
        textoOcupado.setText(texto);
        progreso.setActivo(true);
        filaOcupado.setVisible(true);
        refrescar();
    }

    public void mostrarError(String texto) {
        detenerOcupado();
        mensaje.mostrarError(texto);
        refrescar();
    }

    public void mostrarExito(String texto) {
        detenerOcupado();
        mensaje.mostrarExito(texto);
        refrescar();
    }

    /** Oculta lo que se esté mostrando. */
    public void ocultar() {
        detenerOcupado();
        mensaje.ocultar();
        refrescar();
    }

    /** Texto del mensaje que se está mostrando (vacío si no hay). */
    public String getTexto() {
        return mensaje.getTexto();
    }

    private void detenerOcupado() {
        progreso.setActivo(false);
        filaOcupado.setVisible(false);
    }

    private void refrescar() {
        revalidate();
        repaint();
    }
}
