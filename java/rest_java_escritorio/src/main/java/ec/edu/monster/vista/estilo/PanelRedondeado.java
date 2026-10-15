package ec.edu.monster.vista.estilo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Panel plano con esquinas del radio del tema (encabezados y paneles de mensaje). */
public class PanelRedondeado extends JPanel {

    private final Color relleno;

    public PanelRedondeado(Color relleno) {
        this.relleno = relleno;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(relleno);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Tema.RADIO * 2, Tema.RADIO * 2);
        g2.dispose();
        super.paintComponent(g);
    }

    /** Ocupa todo el ancho disponible, pero nunca más alto que su contenido. */
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
