package ec.edu.monster.servicio;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;
import jakarta.xml.ws.WebServiceException;
import java.util.function.Supplier;

/**
 * Base de todos los servicios del cliente: comparten la {@link Sesion} (token) y la interpretación de las respuestas.
 * <ul>
 *   <li>Una respuesta con estado distinto de OK se convierte en {@link ExcepcionServicio} (código y mensaje del servidor).</li>
 *   <li>Si el servidor exige volver a iniciar sesión (AUTH-002 / AUTH-003) la sesión se descarta.</li>
 *   <li>Un fallo de red o de protocolo (WebServiceException) se convierte en {@link ExcepcionServicio#RED}.</li>
 * </ul>
 *
 * DOMINIO: crear un servicio por área funcional que extienda esta clase y use {@code token()}, {@code llamar(...)} y {@code verificar(...)}.
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

    /** Token de la sesión; sin sesión lanza AUTH-002 sin llamar al servidor. */
    protected String token() {
        if (sesion.getToken() == null) {
            throw new ExcepcionServicio("AUTH-002", "Debe iniciar sesión");
        }
        return sesion.getToken();
    }

    /** Ejecuta una llamada al servidor traduciendo los fallos de red a {@link ExcepcionServicio}. */
    protected <T> T llamar(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (WebServiceException e) {
            throw new ExcepcionServicio(ExcepcionServicio.RED, "No fue posible comunicarse con el servidor", e);
        }
    }

    /** Devuelve la respuesta si estado = OK; si no, lanza ExcepcionServicio con el código y mensaje del servidor. */
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
        return s == null || s.isBlank();
    }
}
