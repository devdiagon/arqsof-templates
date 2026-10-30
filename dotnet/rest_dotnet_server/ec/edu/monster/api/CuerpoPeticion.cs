using System.Net.Http.Headers;
using System.Text;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.api;

/// <summary>
/// Lectura del cuerpo de una petición con cuerpo JSON: exige <c>Content-Type: application/json</c>
/// (si no, ERR-415, como el <c>@Consumes</c> de JAX-RS) y entrega el texto para <see cref="CuerpoJson"/>.
/// </summary>
public static class CuerpoPeticion
{
    private const string TipoJson = "application/json";

    /// <exception cref="ExcepcionMonster">ERR-415 si el Content-Type no es application/json.</exception>
    public static async Task<string> LeerTexto(HttpRequest peticion)
    {
        if (!EsJson(peticion.ContentType))
        {
            throw new ExcepcionMonster(CodigoError.ERR_415);
        }
        using var lector = new StreamReader(peticion.Body, Encoding.UTF8);
        return await lector.ReadToEndAsync();
    }

    public static bool EsJson(string? contentType) =>
        MediaTypeHeaderValue.TryParse(contentType, out MediaTypeHeaderValue? tipo)
        && string.Equals(tipo.MediaType, TipoJson, StringComparison.OrdinalIgnoreCase);
}
