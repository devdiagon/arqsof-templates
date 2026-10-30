using ec.edu.monster.dto;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.servicios;

/// <summary>
/// Base de los servicios del dominio: verifica la envoltura de la respuesta (R5 de docs/CONVENCION_REST.md).
/// No captura excepciones de red ni conoce el protocolo: eso es de <c>api</c>. Es idéntica en SOAP y en REST.
/// </summary>
public abstract class ServicioBase
{
    /// <summary>
    /// Devuelve la respuesta si su estado es OK. Si no, lanza <see cref="ExcepcionServicio"/> con el código y el mensaje
    /// del servidor. Una respuesta nula se trata como respuesta ilegible (<c>SRV-000</c>).
    /// </summary>
    protected static R Verificar<R>(R? respuesta) where R : RespuestaBaseDto
    {
        if (respuesta is null)
        {
            throw new ExcepcionServicio(ExcepcionServicio.Srv000, ExcepcionServicio.MensajeSrv000);
        }
        if (!respuesta.EsOk)
        {
            throw new ExcepcionServicio(respuesta.Codigo ?? ExcepcionServicio.Srv000, respuesta.Mensaje ?? ExcepcionServicio.MensajeSrv000);
        }
        return respuesta;
    }
}
