package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ServicioAutenticacion;
import ec.edu.monster.vista.LoginVista;

/**
 * Controlador de la pantalla de login. No sabe a dónde se navega después: avisa con {@code alAutenticar}
 * y el arranque decide.
 */
public class LoginControlador {

    private final ServicioAutenticacion servicio;
    private final LoginVista vista;
    private final Runnable alAutenticar;

    public LoginControlador(ServicioAutenticacion servicio, LoginVista vista, Runnable alAutenticar) {
        this.servicio = servicio;
        this.vista = vista;
        this.alAutenticar = alAutenticar;
        vista.alPulsarEntrar(e -> iniciarSesion());
    }

    private void iniciarSesion() {
        String usuario = vista.getUsuario();
        String contrasenia = vista.getContrasenia();
        if (usuario.isEmpty() || contrasenia.isEmpty()) {
            vista.mostrarError("Ingrese usuario y contraseña");
            return;
        }
        vista.ocultarMensaje();
        vista.setOcupado(true);
        EjecutorAsincrono.ejecutar(
                () -> {
                    servicio.login(usuario, contrasenia);
                    return Boolean.TRUE;
                },
                ok -> {
                    vista.setOcupado(false);
                    vista.limpiarContrasenia();
                    vista.mostrarExito("Sesión iniciada");
                    alAutenticar.run();
                },
                error -> {
                    vista.setOcupado(false);
                    vista.limpiarContrasenia();
                    vista.mostrarError(error.getMessage());
                });
    }
}
