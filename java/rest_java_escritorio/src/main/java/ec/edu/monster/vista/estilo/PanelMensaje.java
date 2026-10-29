package ec.edu.monster.vista.estilo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * Panel de mensaje: oculto hasta que haya contenido, con una barra de {@link Tema#BARRA_ESTADO}
 * a la izquierda que refuerza el estado (error o éxito). El texto se entiende sin el color.
 */
public class PanelMensaje extends PanelRedondeado {

    private static final int ANCHO_TEXTO = Tema.ANCHO_COLUMNA - 2 * Tema.PADDING_PANEL_MENSAJE - Tema.BARRA_ESTADO - 8;

    private final JLabel etiqueta = new JLabel();
    private Color barra = Tema.ERROR;
    private String texto = "";
    private boolean resultado;

    /** Panel sobre la imagen de fondo de la aplicación (§6). */
    public PanelMensaje() {
        this(Tema.PANEL_SOBRE_IMAGEN);
    }

    /** Panel con el fondo indicado ({@code SUPERFICIE} en la pantalla de acceso, que no tiene imagen debajo). */
    public PanelMensaje(Color fondo) {
        super(fondo);
        setLayout(new java.awt.BorderLayout());
        setBorder(new EmptyBorder(Tema.PADDING_PANEL_MENSAJE, Tema.PADDING_PANEL_MENSAJE + Tema.BARRA_ESTADO,
                Tema.PADDING_PANEL_MENSAJE, Tema.PADDING_PANEL_MENSAJE));
        etiqueta.setForeground(Tema.TEXTO);
        etiqueta.setFont(EstilosUi.fuente(Font.PLAIN, Tema.TAM_MENSAJE));
        add(etiqueta, java.awt.BorderLayout.CENTER);
        setVisible(false);
    }

    public void mostrarError(String mensaje) {
        resultado = false;
        mostrar(mensaje, Tema.ERROR);
    }

    public void mostrarExito(String mensaje) {
        resultado = false;
        mostrar(mensaje, Tema.EXITO);
    }

    /** Resultado: tamaño de resultado, centrado, altura mínima 72. */
    public void mostrarResultado(String mensaje) {
        resultado = true;
        mostrar(mensaje, Tema.EXITO);
    }

    public void ocultar() {
        texto = "";
        setVisible(false);
    }

    /** Texto plano que se está mostrando (vacío si el panel está oculto). */
    public String getTexto() {
        return texto;
    }

    private void mostrar(String mensaje, Color colorBarra) {
        texto = mensaje == null ? "" : mensaje;
        barra = colorBarra;
        etiqueta.setFont(EstilosUi.fuente(resultado ? Font.BOLD : Font.PLAIN, resultado ? Tema.TAM_RESULTADO : Tema.TAM_MENSAJE));
        // El resultado es una línea corta: texto plano centrado. Los mensajes pueden ser largos: HTML con ancho fijo.
        etiqueta.setHorizontalAlignment(resultado ? javax.swing.SwingConstants.CENTER : javax.swing.SwingConstants.LEFT);
        etiqueta.setText(resultado ? texto
                : "<html><body style='width:" + EstilosUi.anchoHtml(ANCHO_TEXTO) + "px'>" + escaparHtml(texto) + "</body></html>");
        setVisible(true);
        revalidate();
        repaint();
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension d = super.getPreferredSize();
        return resultado ? new java.awt.Dimension(d.width, Math.max(d.height, 72)) : d;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), Tema.RADIO * 2, Tema.RADIO * 2));
        g2.setColor(barra);
        g2.fillRect(0, 0, Tema.BARRA_ESTADO, getHeight());
        g2.dispose();
    }

    /** Los mensajes llegan del servidor: se escapan antes de incrustarlos en HTML. */
    static String escaparHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
