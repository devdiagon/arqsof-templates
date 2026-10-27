package ec.edu.monster.vista;

import ec.edu.monster.vista.estilo.EstilosUi;
import ec.edu.monster.vista.estilo.IndicadorProgreso;
import ec.edu.monster.vista.estilo.PanelMensaje;
import ec.edu.monster.vista.estilo.Tema;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Pantalla de acceso lista para los proyectos que definen autenticación (la plantilla base no la trae: no hay un
 * login común a todo SOA). Es pasiva: el proyecto escribe su controlador y la registra en {@link Pantalla}.
 */
public class LoginVista extends JPanel {

    private final JTextField usuario = EstilosUi.campoTexto(null);
    private final JPasswordField contrasenia = EstilosUi.campoContrasenia();
    private final PanelMensaje mensaje = new PanelMensaje();
    private final JButton entrar = EstilosUi.botonPrimario(Textos.INICIAR_SESION);
    private final IndicadorProgreso progreso = new IndicadorProgreso();

    public LoginVista() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(izquierda(EstilosUi.encabezadoAplicacion(Textos.NOMBRE_APLICACION)));
        add(separacion(Tema.ESPACIO_SECCION));
        add(izquierda(EstilosUi.etiqueta(Textos.USUARIO)));
        add(izquierda(usuario));
        add(separacion(Tema.ESPACIO_BLOQUE));
        add(izquierda(EstilosUi.etiqueta(Textos.CLAVE)));
        add(izquierda(contrasenia));
        add(separacion(Tema.ESPACIO_BLOQUE));
        add(izquierda(mensaje));
        add(separacion(Tema.ESPACIO_SECCION));
        add(izquierda(centrado(entrar)));
        add(separacion(Tema.ESPACIO_BLOQUE));
        add(izquierda(centrado(progreso)));
    }

    // ---- Lectura ----

    public String getUsuario() {
        return usuario.getText().trim();
    }

    public String getContrasenia() {
        return new String(contrasenia.getPassword());
    }

    public JButton getBotonEntrar() {
        return entrar;
    }

    public JTextField getCampoUsuario() {
        return usuario;
    }

    public JPasswordField getCampoContrasenia() {
        return contrasenia;
    }

    public String getTextoMensaje() {
        return mensaje.getTexto();
    }

    // ---- Eventos ----

    /** Registra la acción de entrar: botón, o Enter en cualquiera de los dos campos. */
    public void alPulsarEntrar(ActionListener accion) {
        entrar.addActionListener(accion);
        usuario.addActionListener(accion);
        contrasenia.addActionListener(accion);
    }

    // ---- Estado ----

    public void mostrarError(String texto) {
        mensaje.mostrarError(texto);
    }

    public void mostrarExito(String texto) {
        mensaje.mostrarExito(texto);
    }

    public void ocultarMensaje() {
        mensaje.ocultar();
    }

    public void limpiarContrasenia() {
        contrasenia.setText("");
    }

    public void enfocarUsuario() {
        usuario.requestFocusInWindow();
    }

    /** Mientras hay una llamada en curso: controles deshabilitados e indicador visible (§5.8). */
    public void setOcupado(boolean ocupado) {
        usuario.setEnabled(!ocupado);
        contrasenia.setEnabled(!ocupado);
        entrar.setEnabled(!ocupado);
        progreso.setActivo(ocupado);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Tema.ANCHO_COLUMNA, super.getPreferredSize().height);
    }

    // ---- Auxiliares de disposición ----

    private static <T extends JComponent> T izquierda(T componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        return componente;
    }

    private static Component separacion(int alto) {
        return Box.createVerticalStrut(alto);
    }

    private static JPanel centrado(JComponent componente) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        fila.setOpaque(false);
        fila.add(componente);
        return fila;
    }
}
