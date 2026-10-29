package ec.edu.monster.vista.estilo;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Panel con una imagen decorativa: siempre <b>completa y centrada</b> (nunca deformada ni recortada), sobre el color
 * {@code fondo}, y compacta: ocupa como máximo {@link Tema#ESCALA_IMAGEN} del panel. Se recalcula al cambiar el tamaño. Con opacidad menor que 1 sirve de fondo de la ventana (§6):
 * los hijos no opacos se dibujan encima.
 */
public class PanelImagen extends JPanel {

    private final BufferedImage imagen;
    private final float opacidad;

    /** @param recurso ruta absoluta en el classpath, p. ej. {@code /ec/edu/monster/estaticos/fondo_app.png} */
    public PanelImagen(String recurso) {
        this(recurso, 1f);
    }

    /** @param opacidad 1 = opaca; menor, la imagen se funde con el color de fondo */
    public PanelImagen(String recurso, float opacidad) {
        this.imagen = cargar(recurso);
        this.opacidad = opacidad;
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
        double escala = Tema.ESCALA_IMAGEN
                * Math.min((double) getWidth() / imagen.getWidth(), (double) getHeight() / imagen.getHeight());
        int ancho = (int) Math.ceil(imagen.getWidth() * escala);
        int alto = (int) Math.ceil(imagen.getHeight() * escala);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
        g2.drawImage(imagen, (getWidth() - ancho) / 2, (getHeight() - alto) / 2, ancho, alto, null);
        g2.dispose();
    }
}
