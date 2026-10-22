namespace ec.edu.monster.api;

/// <summary>Transporte HTTP: envía una petición y devuelve código y cuerpo, sin decidir si es éxito o error.</summary>
public interface IClienteApi
{
    /// <param name="ruta">Relativa a la URL base y sin barra inicial (por ejemplo "auth/login").</param>
    /// <param name="cuerpo">Objeto que se envía como JSON; null en las consultas GET.</param>
    /// <param name="token">Token Bearer de esta petición; null si la operación no lo requiere.</param>
    /// <exception cref="excepciones.ExcepcionServicio">Con ServidorNoDisponible si no hubo respuesta.</exception>
    Task<RespuestaHttp> EnviarAsync(HttpMethod metodo, string ruta, object? cuerpo, string? token,
        CancellationToken ct = default);
}
