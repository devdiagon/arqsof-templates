package ec.edu.monster.vista.estilo;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.Timer;

/** Indicador de progreso circular e indeterminado (§5.8), color de acento. Oculto salvo que esté activo. */
public class IndicadorProgreso extends JComponent {

    private static final int DIAMETRO = 40;
    private static final int GROSOR = 4;

    private int angulo;
    private final Timer reloj = new Timer(30, e -> {
        angulo = (angulo + 12) % 360;
        repaint();
    });

    public IndicadorProgreso() {
        setVisible(false);
    }

    public void setActivo(boolean activo) {
        setVisible(activo);
        if (activo) {
            reloj.start();
        } else {
            reloj.stop();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DIAMETRO, DIAMETRO);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Tema.ACENTO);
        g2.setStroke(new BasicStroke(GROSOR, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int d = DIAMETRO - GROSOR;
        g2.drawArc(GROSOR / 2, GROSOR / 2, d, d, -angulo, 270);
        g2.dispose();
    }
}
