package ec.edu.monster.vista.estilo;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** Flecha hacia la derecha de 24 × 24 (color {@code texto}), dibujada en código: indica el sentido de una transferencia. */
public class FlechaDireccion extends JComponent {

    private static final int TAMANO = 24;

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(TAMANO, TAMANO);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Tema.TEXTO);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int y = getHeight() / 2;
        int x0 = (getWidth() - TAMANO) / 2 + 2;
        int x1 = x0 + TAMANO - 4;
        g2.drawLine(x0, y, x1, y);
        g2.drawLine(x1 - 7, y - 7, x1, y);
        g2.drawLine(x1 - 7, y + 7, x1, y);
        g2.dispose();
    }
}
