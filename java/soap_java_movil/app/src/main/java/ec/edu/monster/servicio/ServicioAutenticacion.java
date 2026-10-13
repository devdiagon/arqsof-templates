package ec.edu.monster.servicio;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.ws.ContratoWS;

/**
 * Caso de uso compartido: iniciar sesion. Valida lo trivial (campos vacios) y deja el token en la Sesion.
 *
 * @author: Frederick Tipán
 */
public class ServicioAutenticacion extends ServicioBase {

    private final ContratoWS ws;

    public ServicioAutenticacion(ContratoWS ws, Sesion sesion) {
        super(sesion);
        this.ws = ws;
    }

    public void login(String usuario, String contrasenia) {
        if (esVacio(usuario) || esVacio(contrasenia)) {
            throw new ExcepcionServicio("AUTH-001", "Ingrese usuario y contraseña");
        }
        sesion.setToken(verificar(ws.login(usuario, contrasenia)).getToken());
    }
}
