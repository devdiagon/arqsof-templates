package ec.edu.monster.vista;

import ec.edu.monster.vista.estilo.EstilosUi;
import ec.edu.monster.vista.estilo.Tema;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Pantalla de acceso (docs/PANTALLAS.md §5.1): encabezado · Usuario · Clave · Iniciar sesión · zona de estado.
 * Pasiva: lee lo escrito y avisa de los eventos. Se muestra en la disposición dividida, sin imagen debajo de los paneles.
 */
public class LoginVista extends VistaBase {

    /** Campos de la pantalla, en orden. */
    public enum Campo { USUARIO, CLAVE }

    private final JTextField usuario = EstilosUi.campoTexto(null);
    private final JPasswordField clave = EstilosUi.campoContrasenia();
    private final JButton entrar = EstilosUi.botonPrimario(Textos.INICIAR_SESION);

    public LoginVista() {
        super(Tema.ANCHO_COLUMNA, Tema.SUPERFICIE);
        registrar(usuario, clave, entrar);

        add(izquierda(EstilosUi.encabezadoAplicacion(Textos.NOMBRE_APLICACION)));
        add(separacion(Tema.ESPACIO_SECCION));
        add(izquierda(EstilosUi.etiqueta(Textos.USUARIO)));
        add(izquierda(usuario));
        add(separacion(Tema.ESPACIO_BLOQUE));
        add(izquierda(EstilosUi.etiqueta(Textos.CLAVE)));
        add(izquierda(clave));
        add(separacion(Tema.ESPACIO_SECCION));
        add(centrado(entrar));
        add(separacion(Tema.ESPACIO_BLOQUE));
        add(izquierda(estado));
    }

    // ---- Lectura ----

    public String getUsuario() {
        return usuario.getText().trim();
    }

    public String getClave() {
        return new String(clave.getPassword());
    }

    // ---- Eventos ----

    /** Botón, o Enter en cualquiera de los dos campos. */
    public void alIniciarSesion(Runnable accion) {
        entrar.addActionListener(e -> accion.run());
        usuario.addActionListener(e -> accion.run());
        clave.addActionListener(e -> accion.run());
    }


    // ---- Estado ----

    public void marcarVacio(Campo campo) {
        EstilosUi.marcarError(campo == Campo.USUARIO ? usuario : clave, true);
    }

    public void limpiarClave() {
        clave.setText("");
    }

    @Override
    public void alMostrar() {
        limpiarMarcas();
        clave.setText("");
        estado.ocultar();
        usuario.requestFocusInWindow();
    }

    /** Vuelve al campo de usuario tras un error (el usuario se conserva). */
    public void enfocarUsuario() {
        usuario.requestFocusInWindow();
    }
}
