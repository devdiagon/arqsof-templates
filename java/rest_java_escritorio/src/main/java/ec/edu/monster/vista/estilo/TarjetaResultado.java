package ec.edu.monster.vista.estilo;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Tarjeta de resultado (CLIENT_STYLES_JAVA.md §5.12): resume el cambio de un valor. Blanca, plana, de ancho fijo, con un
 * contorno sutil del color primario. De arriba abajo: título centrado (opcional), monto con signo (verde si suma, rojo
 * si resta), variación, un divisor y el pie a la derecha.
 * Recibe los textos ya preparados: no conoce el dominio ni el formato de los importes.
 */
public class TarjetaResultado extends PanelRedondeado {

    private static final int ANCHO_TEXTO = Tema.ANCHO_TARJETA - 2 * Tema.PADDING_TARJETA;

    public TarjetaResultado(String titulo, boolean suma, String monto, String variacion, String pie) {
        super(Tema.CAMPO_FONDO);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(Tema.PADDING_TARJETA, Tema.PADDING_TARJETA, Tema.PADDING_TARJETA, Tema.PADDING_TARJETA));

        JPanel cuerpo = new JPanel();
        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        if (titulo != null && !titulo.isBlank()) {
            JLabel encabezado = new JLabel(titulo, SwingConstants.CENTER);
            encabezado.setFont(fuenteTitulo());
            encabezado.setForeground(Tema.CAMPO_PLACEHOLDER);
            encabezado.setAlignmentX(Component.LEFT_ALIGNMENT);
            Dimension tamano = new Dimension(ANCHO_TEXTO, encabezado.getPreferredSize().height + 4);
            encabezado.setPreferredSize(tamano);   // ocupa todo el ancho útil para centrar el texto
            encabezado.setMaximumSize(tamano);
            cuerpo.add(encabezado);
        }
        JLabel importe = texto(monto, Tema.TAM_TITULO_PANTALLA, Font.BOLD, suma ? Tema.EXITO_CLARO : Tema.ERROR_CLARO, false);
        importe.setBorder(new EmptyBorder(8, 0, 8, 0));
        cuerpo.add(importe);
        cuerpo.add(texto(variacion, Tema.TAM_CUERPO, Font.PLAIN, Tema.CAMPO_TEXTO, false));
        add(cuerpo, BorderLayout.CENTER);

        JPanel piePanel = new JPanel(new BorderLayout());
        piePanel.setOpaque(false);
        piePanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        JPanel divisor = new JPanel();
        divisor.setBackground(Tema.DIVISOR_TARJETA);
        divisor.setPreferredSize(new Dimension(0, 1));
        piePanel.add(divisor, BorderLayout.NORTH);
        JLabel piePagina = new JLabel(pie, SwingConstants.RIGHT);
        piePagina.setFont(EstilosUi.fuente(Font.PLAIN, Tema.TAM_MENSAJE));
        piePagina.setForeground(Tema.CAMPO_PLACEHOLDER);
        piePagina.setBorder(new EmptyBorder(8, 0, 0, 0));
        piePanel.add(piePagina, BorderLayout.CENTER);
        add(piePanel, BorderLayout.SOUTH);
    }

    /** Texto con ajuste de línea al ancho útil de la tarjeta, centrado o a la izquierda. */
    private static JLabel texto(String contenido, float tamano, int estilo, Color color, boolean centrado) {
        String alineacion = centrado ? "center" : "left";
        JLabel etiqueta = new JLabel("<html><div style='width:" + EstilosUi.anchoHtml(ANCHO_TEXTO) + "px; text-align:" + alineacion + "'>"
                + escapar(contenido) + "</div></html>", centrado ? SwingConstants.CENTER : SwingConstants.LEFT);
        etiqueta.setFont(EstilosUi.fuente(estilo, tamano));
        etiqueta.setForeground(color);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    /** Peso intermedio (casi negrita): «Segoe UI Semibold» si existe; si no, negrita. */
    private static Font fuenteTitulo() {
        Font semibold = new Font("Segoe UI Semibold", Font.PLAIN, 1);
        return "Segoe UI Semibold".equals(semibold.getFamily())
                ? semibold.deriveFont(Tema.TAM_CUERPO)
                : EstilosUi.fuente(Font.BOLD, Tema.TAM_CUERPO);
    }

    private static String escapar(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Tema.CONTORNO_TARJETA);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, Tema.RADIO * 2, Tema.RADIO * 2);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Tema.ANCHO_TARJETA, super.getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
