package ec.edu.monster.servicio;

import ec.edu.monster.api.ClienteApi;
import ec.edu.monster.api.ConfiguracionApi;
import ec.edu.monster.dto.RespuestaLoginDto;
import ec.edu.monster.dto.SolicitudLoginDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;

/**
 * Caso de uso compartido: iniciar sesion. Valida lo trivial (campos vacios) y deja el token en la Sesion.
 *
 * @author: Frederick Tipán
 */
public class ServicioAutenticacion extends ServicioBase {

    public ServicioAutenticacion(ClienteApi api, Sesion sesion) {
        super(api, sesion);
    }

    public void login(String usuario, String contrasenia) {
        if (esVacio(usuario) || esVacio(contrasenia)) {
            throw new ExcepcionServicio("AUTH-001", "Ingrese usuario y contraseña");
        }
        RespuestaLoginDto respuesta = postPublico(ConfiguracionApi.RUTA_LOGIN,
                new SolicitudLoginDto(usuario, contrasenia), RespuestaLoginDto.class);
        if (esVacio(respuesta.getToken())) {
            throw new ExcepcionServicio("SRV-000", "El servidor no devolvió el token");
        }
        sesion.setToken(respuesta.getToken());
    }
}
