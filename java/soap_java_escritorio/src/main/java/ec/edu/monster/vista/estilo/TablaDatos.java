package ec.edu.monster.vista.estilo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Tabla (CLIENT_STYLES_JAVA.md §5.13): panel sobre la imagen, encabezado en negrita con una línea debajo, filas de
 * {@link Tema#ALTO_FILA_TABLA} separadas por una línea, sin cuadrícula vertical ni selección. Las columnas numéricas
 * van a la derecha. Si hay más filas de {@link Tema#FILAS_VISIBLES_TABLA} se desplaza por dentro con el encabezado fijo.
 */
public class TablaDatos extends PanelRedondeado {

    private final JTable tabla = new JTable();
    private final JScrollPane desplazamiento = new JScrollPane(tabla);
    private boolean[] derecha = new boolean[0];
    private int[] pesos = new int[0];

    public TablaDatos() {
        super(Tema.PANEL_SOBRE_IMAGEN);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(Tema.PADDING_PANEL_MENSAJE, Tema.PADDING_PANEL_MENSAJE,
                Tema.PADDING_PANEL_MENSAJE, Tema.PADDING_PANEL_MENSAJE));

        tabla.setOpaque(false);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setRowHeight(Tema.ALTO_FILA_TABLA);
        tabla.setRowSelectionAllowed(false);
        tabla.setCellSelectionEnabled(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFocusable(false);
        tabla.setFillsViewportHeight(false);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tabla.setFont(EstilosUi.fuente(Font.PLAIN, Tema.TAM_CUERPO));
        tabla.setForeground(Tema.TEXTO);
        tabla.setDefaultRenderer(Object.class, new Celda(false));

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setReorderingAllowed(false);
        encabezado.setResizingAllowed(false);
        encabezado.setOpaque(false);
        encabezado.setBackground(Tema.PANEL_SOBRE_IMAGEN);

        desplazamiento.setOpaque(false);
        desplazamiento.getViewport().setOpaque(false);
        desplazamiento.setBorder(null);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(Tema.ALTO_FILA_TABLA);
        add(desplazamiento, BorderLayout.CENTER);
        desplazamiento.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repartirAnchos();
            }
        });
    }

    /**
     * Reemplaza el contenido.
     *
     * @param columnas títulos de las columnas
     * @param alDerecha por columna: {@code true} si va alineada a la derecha (numéricas)
     * @param anchos    peso relativo del ancho de cada columna
     * @param filas     una por registro, con un texto por columna
     */
    public void setDatos(String[] columnas, boolean[] alDerecha, int[] anchos, List<String[]> filas) {
        this.pesos = anchos.clone();
        this.derecha = alDerecha.clone();
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        filas.forEach(modelo::addRow);
        tabla.setModel(modelo);
        for (int i = 0; i < columnas.length; i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(new Celda(false));
            tabla.getColumnModel().getColumn(i).setHeaderRenderer(new Celda(true));
        }
        int visibles = Math.min(Math.max(filas.size(), 1), Tema.FILAS_VISIBLES_TABLA);
        int alto = visibles * Tema.ALTO_FILA_TABLA + Tema.ALTO_FILA_TABLA;   // filas + encabezado
        desplazamiento.setPreferredSize(new Dimension(0, alto + 2));
        repartirAnchos();
        revalidate();
        repaint();
    }

    /** Reparte el ancho disponible entre las columnas según sus pesos. */
    private void repartirAnchos() {
        int columnas = tabla.getColumnCount();
        int total = desplazamiento.getViewport().getWidth();
        int suma = 0;
        for (int i = 0; i < columnas && i < pesos.length; i++) {
            suma += pesos[i];
        }
        if (total <= 0 || suma == 0) {
            return;
        }
        for (int i = 0; i < columnas && i < pesos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(total * pesos[i] / suma);
        }
    }

    /** Celda y encabezado: línea inferior de {@code borde}, texto del tema y alineación según la columna. */
    private final class Celda extends DefaultTableCellRenderer {

        private final boolean encabezado;

        Celda(boolean encabezado) {
            this.encabezado = encabezado;
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada, boolean foco,
                int fila, int columna) {
            JLabel etiqueta = (JLabel) super.getTableCellRendererComponent(t, valor, false, false, fila, columna);
            etiqueta.setOpaque(false);
            etiqueta.setForeground(Tema.TEXTO);
            etiqueta.setFont(EstilosUi.fuente(encabezado ? Font.BOLD : Font.PLAIN,
                    encabezado ? Tema.TAM_ETIQUETA : Tema.TAM_CUERPO));
            etiqueta.setHorizontalAlignment(columna < derecha.length && derecha[columna]
                    ? SwingConstants.RIGHT : SwingConstants.LEFT);
            etiqueta.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Tema.BORDE),
                    new EmptyBorder(0, 8, 0, 8)));
            return etiqueta;
        }
    }
}
