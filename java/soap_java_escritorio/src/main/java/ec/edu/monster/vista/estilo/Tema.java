package ec.edu.monster.vista.estilo;

import java.awt.Color;

public final class Tema {

    private Tema() {
    }

    // Marca y acción
    public static final Color ACENTO = new Color(0x0EA5E9);
    public static final Color PRIMARIO = new Color(0x0369A1);
    public static final Color PRIMARIO_PRESIONADO = new Color(0x075985);
    public static final Color SECUNDARIO = new Color(0x475569);
    public static final Color SECUNDARIO_PRESIONADO = new Color(0x334155);
    public static final Color SOBRE_ACCION = Color.WHITE;

    // Presentación oscura
    public static final Color FONDO = new Color(0x111E2F);
    public static final Color SUPERFICIE = new Color(0x1B2B42);
    public static final Color TEXTO = new Color(0xF1F5F9);
    public static final Color TEXTO_SECUNDARIO = new Color(0x94A3B8);
    public static final Color BORDE = new Color(0x2D3F5A);
    public static final Color ERROR = new Color(0xF87171);
    public static final Color EXITO = new Color(0x4ADE80);

    // Formulario
    public static final Color CAMPO_FONDO = Color.WHITE;
    public static final Color CAMPO_TEXTO = new Color(0x0F172A);
    public static final Color CAMPO_PLACEHOLDER = new Color(0x64748B);

    // Medidas: unidad base 4
    public static final int RADIO = 8;
    public static final int ALTURA_CONTROL = 36;
    public static final int MARGEN_PANTALLA = 24;
    public static final int ESPACIO_BLOQUE = 16;
    public static final int ESPACIO_SECCION = 24;
    public static final int ESPACIO_ACCIONES_SALIDA = 32;
    public static final int PADDING_PANEL_TITULO = 16;
    public static final int PADDING_PANEL_MENSAJE = 12;
    public static final int PADDING_CAMPO = 12;
    public static final int SEPARACION_ETIQUETA = 6;
    public static final int BARRA_ESTADO = 4;

    // Dentro de la aplicación (CLIENT_STYLES_JAVA.md §5.11, §5.12, §5.13, §6, §7.0)
    public static final Color PANEL_SOBRE_IMAGEN = new Color(0, 0, 0, 153);   // rgba(0,0,0,0.60)
    public static final float OPACIDAD_IMAGEN = 0.40f;
    public static final Color EXITO_CLARO = new Color(0x15803D);   // sobre blanco (tarjeta)
    public static final Color ERROR_CLARO = new Color(0xB91C1C);   // sobre blanco (tarjeta)
    public static final int ALTO_BOTON_CUADRICULA = 96;
    public static final int ANCHO_TARJETA = 260;
    public static final int PADDING_TARJETA = 16;
    public static final Color DIVISOR_TARJETA = new Color(0xCBD5E1);              // borde de la presentación clara
    public static final Color CONTORNO_TARJETA = new Color(3, 105, 161, 130);     // primario, sutil
    public static final int ALTO_FILA_TABLA = 36;
    public static final int FILAS_VISIBLES_TABLA = 10;
    public static final int ANCHO_CONTENIDO_FORMULARIO = 480;
    public static final int ANCHO_CONTENIDO_AMPLIO = 640;

    // Ventana y columna de contenido
    // Ventana: formulario a la izquierda (ancho fijo) e imagen a la derecha (el resto)
    public static final int VENTANA_ANCHO = 900;
    public static final int VENTANA_ALTO = 640;
    public static final int VENTANA_ANCHO_INICIAL = 1100;
    public static final int VENTANA_ALTO_INICIAL = 720;
    public static final int ANCHO_PANEL_FORMULARIO = 560;
    public static final int ANCHO_COLUMNA = 420;

    // Tamaños de fuente por rol, escritorio (§3)
    public static final float TAM_TITULO_APLICACION = 28f;
    public static final float TAM_TITULO_PANTALLA = 24f;
    public static final float TAM_RESULTADO = 18f;
    public static final float TAM_CUERPO = 14f;
    public static final float TAM_ETIQUETA = 13f;
    public static final float TAM_MENSAJE = 13f;
    public static final float TAM_BOTON = 15f;

    /** Color resultante de dibujar {@code color} con opacidad {@code alfa} sobre {@code fondo} (estados deshabilitados: 60 %). */
    public static Color mezclar(Color color, Color fondo, double alfa) {
        int r = (int) Math.round(color.getRed() * alfa + fondo.getRed() * (1 - alfa));
        int g = (int) Math.round(color.getGreen() * alfa + fondo.getGreen() * (1 - alfa));
        int b = (int) Math.round(color.getBlue() * alfa + fondo.getBlue() * (1 - alfa));
        return new Color(r, g, b);
    }

    /** Formato #RRGGBB para las cadenas de estilo de FlatLaf. */
    public static String hex(Color color) {
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }
}
