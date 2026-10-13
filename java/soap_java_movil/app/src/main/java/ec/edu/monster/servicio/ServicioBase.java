package ec.edu.monster.servicio;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;

/**
 * Base de todos los servicios del cliente: comparten la Sesion (token) y la interpretacion de las respuestas.
 * Reglas: una respuesta con estado distinto de OK se convierte en ExcepcionServicio (codigo y mensaje del servidor);
 * si el servidor exige volver a iniciar sesion (AUTH-002 / AUTH-003) la sesion se descarta.
 *
 * DOMINIO: crear un servicio por area funcional que extienda esta clase y use token(), verificar(...).
 *
 * @author: Frederick Tipán
 */
public abstract class ServicioBase {

    protected final Sesion sesion;

    protected ServicioBase(Sesion sesion) {
        this.sesion = sesion;
    }

    public boolean haySesion() {
        return sesion.getToken() != null;
    }

    public void cerrarSesion() {
        sesion.setToken(null);
    }

    /** Token de la sesion; sin sesion lanza AUTH-002 sin llamar al servidor. */
    protected String token() {
        if (sesion.getToken() == null) {
            throw new ExcepcionServicio("AUTH-002", "Debe iniciar sesión");
        }
        return sesion.getToken();
    }

    /** Devuelve la respuesta si estado = OK; si no, lanza ExcepcionServicio con el codigo y mensaje del servidor. */
    protected <T extends RespuestaBaseDto> T verificar(T respuesta) {
        if (respuesta == null) {
            throw new ExcepcionServicio("SRV-000", "El servidor no devolvió respuesta");
        }
        if (!respuesta.esOk()) {
            ExcepcionServicio e = new ExcepcionServicio(respuesta.getCodigo(), respuesta.getMensaje());
            if (e.exigeLogin()) {
                sesion.setToken(null);
            }
            throw e;
        }
        return respuesta;
    }

    protected static boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
