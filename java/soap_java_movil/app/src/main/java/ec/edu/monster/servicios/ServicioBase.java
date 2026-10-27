package ec.edu.monster.servicios;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;

/**
 * Base de los servicios del dominio: verifica la envoltura de la respuesta (R6 de docs/CONVENCION_SOAP.md).
 * No captura excepciones de red ni conoce el protocolo: eso es de {@code ws}. Es idéntica en SOAP y en REST.
 *
 * DOMINIO: crear un servicio por área funcional que extienda esta clase, reciba la interfaz del cliente ({@code XCliente})
 * y devuelva {@code verificar(cliente.operacion(...))}.
 */
public abstract class ServicioBase {

    /**
     * Devuelve la respuesta si su estado es OK. Si no, lanza {@link ExcepcionServicio} con el código y el mensaje
     * del servidor. Una respuesta nula se trata como respuesta ilegible ({@code SRV-000}).
     */
    protected <R extends RespuestaBaseDto> R verificar(R respuesta) {
        if (respuesta == null) {
            throw new ExcepcionServicio(ExcepcionServicio.SRV_000, ExcepcionServicio.MENSAJE_SRV_000);
        }
        if (!respuesta.esOk()) {
            throw new ExcepcionServicio(respuesta.getCodigo(), respuesta.getMensaje());
        }
        return respuesta;
    }
}
