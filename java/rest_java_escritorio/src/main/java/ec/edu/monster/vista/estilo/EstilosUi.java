package ec.edu.monster.vista.estilo;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * Fábrica de componentes. Las vistas crean todos sus componentes
 * aquí y no colorean ni dimensionan nada por su cuenta.
 */
public final class EstilosUi {

    private static double factorHtml;

    private EstilosUi() {
    }

    /** Debe llamarse una vez al arrancar, ANTES de crear cualquier componente. */
    public static void instalarLookAndFeel() {
        FlatDarkLaf.setup();
        UIManager.put("Component.arc", Tema.RADIO);
        UIManager.put("Button.arc", Tema.RADIO);
        UIManager.put("TextComponent.arc", Tema.RADIO);
        UIManager.put("Component.focusColor", Tema.ACENTO);
        UIManager.put("Panel.background", Tema.FONDO);
        UIManager.put("Label.foreground", Tema.TEXTO);
    }

    /**
     * Valor de {@code width} (en px de HTML) con el que un texto HTML mide {@code ancho} píxeles reales. Swing escala los
     * px de HTML según la resolución de la pantalla (p. ej. 125 %), así que un ancho fijo en el HTML no coincide con el
     * del componente y el texto se corta; se mide una vez con una etiqueta de prueba.
     */
    public static int anchoHtml(int ancho) {
        if (factorHtml == 0) {
            int medida = new JLabel("<html><div style='width:1000px'>x</div></html>").getPreferredSize().width;
            factorHtml = medida > 0 ? medida / 1000.0 : 1.0;
        }
        return (int) Math.floor(ancho / factorHtml);
    }

    /** Fuente del look and feel (no se fuerza otra familia) con el estilo y tamaño del rol. */
    public static Font fuente(int estilo, float tamano) {
        Font base = UIManager.getFont("Label.font");
        return base.deriveFont(estilo, tamano);
    }

    // ---- Encabezados y etiquetas ----

    /** Encabezado del login: título de aplicación, centrado. */
    public static PanelRedondeado encabezadoAplicacion(String texto) {
        return encabezado(texto, Tema.TAM_TITULO_APLICACION, SwingConstants.CENTER, Tema.SUPERFICIE);
    }

    /** Encabezado de pantalla: título de pantalla, a la izquierda. */
    public static PanelRedondeado encabezadoPantalla(String texto) {
        return encabezado(texto, Tema.TAM_TITULO_PANTALLA, SwingConstants.LEFT, Tema.PANEL_SOBRE_IMAGEN);
    }

    private static PanelRedondeado encabezado(String texto, float tamano, int alineacion, Color fondo) {
        PanelRedondeado panel = new PanelRedondeado(fondo);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(Tema.PADDING_PANEL_TITULO, Tema.PADDING_PANEL_TITULO,
                Tema.PADDING_PANEL_TITULO, Tema.PADDING_PANEL_TITULO));
        JLabel titulo = new JLabel(texto, alineacion);
        titulo.setFont(fuente(Font.BOLD, tamano));
        titulo.setForeground(Tema.TEXTO);
        panel.add(titulo, BorderLayout.CENTER);
        return panel;
    }

    /** Etiqueta de campo: encima del campo, separada 6 de él. */
    public static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente(Font.BOLD, Tema.TAM_ETIQUETA));
        etiqueta.setForeground(Tema.TEXTO);
        etiqueta.setBorder(new EmptyBorder(0, 0, Tema.SEPARACION_ETIQUETA, 0));
        return etiqueta;
    }

    // ---- Campos ----

    /** Campo de texto: con estilo y placeholder. */
    public static JTextField campoTexto(String placeholder) {
        JTextField campo = new JTextField();
        estilizarCampo(campo, placeholder);
        return campo;
    }

    /** Campo de contraseña: oculta el contenido. */
    public static JPasswordField campoContrasenia() {
        JPasswordField campo = new JPasswordField();
        estilizarCampo(campo, null);
        return campo;
    }

    private static void estilizarCampo(JTextField campo, String placeholder) {
        campo.setFont(fuente(Font.PLAIN, Tema.TAM_CUERPO));
        if (placeholder != null) {
            campo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        }
        // Sin borde visible en reposo (borde del mismo color que el fondo); al enfocar, borde de 2 en acento.
        campo.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + Tema.RADIO
                + "; background:" + Tema.hex(Tema.CAMPO_FONDO)
                + "; foreground:" + Tema.hex(Tema.CAMPO_TEXTO)
                + "; caretColor:" + Tema.hex(Tema.CAMPO_TEXTO)
                + "; placeholderForeground:" + Tema.hex(Tema.CAMPO_PLACEHOLDER)
                + "; borderWidth:2"
                + "; borderColor:" + Tema.hex(Tema.CAMPO_FONDO)
                + "; focusedBorderColor:" + Tema.hex(Tema.ACENTO)
                + "; focusWidth:0"
                + "; margin:" + Tema.PADDING_CAMPO + "," + Tema.PADDING_CAMPO + ","
                + Tema.PADDING_CAMPO + "," + Tema.PADDING_CAMPO);
        // La altura mínima de control (36) la garantiza el padding: FlatLaf no admite minimumHeight en campos.
    }

    /** Selector: lista desplegable con el aspecto de un campo de texto. */
    public static JComboBox<String> selector() {
        JComboBox<String> selector = new JComboBox<>();
        selector.setFont(fuente(Font.PLAIN, Tema.TAM_CUERPO));
        selector.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + Tema.RADIO
                + "; background:" + Tema.hex(Tema.CAMPO_FONDO)
                + "; foreground:" + Tema.hex(Tema.CAMPO_TEXTO)
                + "; borderWidth:2"
                + "; borderColor:" + Tema.hex(Tema.CAMPO_FONDO)
                + "; focusedBorderColor:" + Tema.hex(Tema.ACENTO)
                + "; focusWidth:0"
                + "; buttonBackground:" + Tema.hex(Tema.CAMPO_FONDO)
                + "; buttonArrowColor:#424242"
                + "; padding:" + Tema.PADDING_CAMPO + "," + Tema.PADDING_CAMPO + ","
                + Tema.PADDING_CAMPO + "," + Tema.PADDING_CAMPO);
        return selector;
    }

    /** Marca (o desmarca) un campo con el borde de error. */
    public static void marcarError(JComponent campo, boolean conError) {
        campo.putClientProperty(FlatClientProperties.OUTLINE, conError ? Tema.ERROR : null);
    }

    // ---- Botones ----

    /** Botón primario: la acción principal de la pantalla. */
    public static JButton botonPrimario(String texto) {
        return boton(texto, Tema.PRIMARIO, Tema.PRIMARIO_PRESIONADO);
    }

    /** Botón secundario sólido: acciones de salida o de regreso. */
    public static JButton botonSecundario(String texto) {
        return boton(texto, Tema.SECUNDARIO, Tema.SECUNDARIO_PRESIONADO);
    }

    /** Botón primario grande de la cuadrícula de acciones (§5.11). */
    public static JButton botonPrimarioGrande(String texto) {
        JButton boton = botonPrimario(texto);
        boton.setPreferredSize(new java.awt.Dimension(0, Tema.ALTO_BOTON_CUADRICULA));
        return boton;
    }

    private static JButton boton(String texto, Color relleno, Color presionado) {
        JButton boton = new JButton(texto);
        boton.setFont(fuente(Font.BOLD, Tema.TAM_BOTON));
        boton.setFocusPainted(false);
        boton.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + Tema.RADIO
                + "; background:" + Tema.hex(relleno)
                + "; foreground:" + Tema.hex(Tema.SOBRE_ACCION)
                + "; hoverBackground:" + Tema.hex(presionado)
                + "; pressedBackground:" + Tema.hex(presionado)
                + "; disabledBackground:" + Tema.hex(Tema.mezclar(relleno, Tema.FONDO, 0.6))
                + "; disabledText:" + Tema.hex(Tema.mezclar(Tema.SOBRE_ACCION, Tema.FONDO, 0.6))
                + "; borderWidth:0"
                + "; margin:8,20,8,20"
                + "; minimumHeight:" + Tema.ALTURA_CONTROL);
        return boton;
    }
}
