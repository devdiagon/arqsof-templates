package ec.edu.monster.servicio;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.ws.ClienteServicio;

/** Caso de uso compartido: iniciar sesión. Valida lo trivial (campos vacíos) y deja el token en la Sesion. */
public class ServicioAutenticacion extends ServicioBase {

    private final ClienteServicio ws;

    public ServicioAutenticacion(ClienteServicio ws, Sesion sesion) {
        super(sesion);
        this.ws = ws;
    }

    public void login(String usuario, String contrasenia) {
        if (esVacio(usuario) || esVacio(contrasenia)) {
            throw new ExcepcionServicio("AUTH-001", "Ingrese usuario y contraseña");
        }
        sesion.setToken(verificar(llamar(() -> ws.login(usuario, contrasenia))).getToken());
    }
}
