namespace ec.edu.monster.api;

/// <summary>
/// Envía una petición HTTP y devuelve la respuesta cruda. Es lo único que toca la red; los clientes <c>…ClienteApi</c> lo
/// reciben por el constructor, así las pruebas usan un transporte falso, sin servidor ni red.
/// Un HTTP de error (4xx, 5xx) NO es una excepción: se devuelve como cualquier otra respuesta, porque el cuerpo lleva la
/// envoltura con el mensaje del servidor. Solo la falta de respuesta (red, tiempo agotado) lanza una excepción.
/// </summary>
public interface ITransporte
{
    /// <param name="metodo"><c>GET</c> o <c>POST</c>.</param>
    /// <param name="ruta">Relativa a la URL base y sin barra inicial; sus segmentos ya vienen codificados.</param>
    /// <param name="cuerpoJson">Cuerpo JSON, o <c>null</c> si la operación no lleva.</param>
    /// <param name="autorizacion">Valor completo de la cabecera <c>Authorization</c>, o <c>null</c> si la operación no la lleva.</param>
    Task<RespuestaHttp> EnviarAsync(string metodo, string ruta, string? cuerpoJson, string? autorizacion);
}
