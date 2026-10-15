package ec.edu.monster.vista.estilo;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Panel decorativo con una imagen: se muestra completa y centrada
 */
public class PanelImagen extends JPanel {

    private final BufferedImage imagen;

    /** @param recurso ruta absoluta en el classpath, p. ej. {@code /ec/edu/monster/estaticos/fondo_app.jpeg} */
    public PanelImagen(String recurso) {
        this.imagen = cargar(recurso);
        setBackground(Tema.FONDO);
        setOpaque(true);
    }

    private static BufferedImage cargar(String recurso) {
        try (InputStream in = PanelImagen.class.getResourceAsStream(recurso)) {
            return in == null ? null : ImageIO.read(in);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imagen == null) {
            return;
        }
        double escala = Math.min((double) getWidth() / imagen.getWidth(), (double) getHeight() / imagen.getHeight());
        int ancho = (int) Math.ceil(imagen.getWidth() * escala);
        int alto = (int) Math.ceil(imagen.getHeight() * escala);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(imagen, (getWidth() - ancho) / 2, (getHeight() - alto) / 2, ancho, alto, null);
        g2.dispose();
    }
}
