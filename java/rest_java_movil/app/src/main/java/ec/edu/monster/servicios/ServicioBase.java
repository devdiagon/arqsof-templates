package ec.edu.monster.servicios;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;

/**
 * Base de los servicios del dominio: verifica la envoltura de la respuesta (R5 de docs/CONVENCION_REST.md).
 * No captura excepciones de red ni conoce el protocolo: eso es de {@code api}.
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
