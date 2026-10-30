using System.Text;
using System.Text.Encodings.Web;
using System.Text.Json;
using ec.edu.monster.dto;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.api;

/// <summary>
/// Arma las peticiones y lee las respuestas, sin tocar la red (se prueba con un <see cref="ITransporte"/> falso). Es el
/// equivalente REST de <c>TraductorRed</c>: ninguna capa superior ve una excepción de HTTP o de JSON.
/// <list type="bullet">
/// <item>Sin respuesta (servidor caído, red, tiempo agotado) → <c>RED</c>.</item>
/// <item>Cuerpo vacío, que no es JSON o sin <c>estado</c> (página de un proxy, <c>502</c>) → <c>SRV-000</c>.</item>
/// <item>Se lee la envoltura también cuando el HTTP es de error: el <c>mensaje</c> del servidor nunca se descarta y
/// decide <c>ServicioBase</c>.</item>
/// </list>
/// </summary>
public static class TraductorApi
{
    /// <summary>
    /// Mapeo JSON ↔ DTO (ARQUITECTURA_CLIENTE.md 4.5): los DTO no llevan anotaciones. Nombres en camelCase, lectura sin
    /// distinguir mayúsculas (R4) y <c>Populate</c>, sin el cual las colecciones de solo lectura (<c>Movimientos</c>) no
    /// se rellenan. Los campos desconocidos se ignoran.
    /// </summary>
    internal static readonly JsonSerializerOptions Opciones = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
        PropertyNameCaseInsensitive = true,
        PreferredObjectCreationHandling = System.Text.Json.Serialization.JsonObjectCreationHandling.Populate,
        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping
    };

    /// <summary>Codifica una variable de ruta (R10): los espacios salen como %20 y «/» como %2F.</summary>
    public static string Codificar(string segmento) => Uri.EscapeDataString(segmento);

    /// <summary>Cuerpo JSON de un objeto con campos de texto (R9): <c>Cuerpo("campo", "valor")</c>. Pares nombre, valor.</summary>
    public static string Cuerpo(params string[] paresNombreValor)
    {
        var campos = new Dictionary<string, string>();
        for (int i = 0; i + 1 < paresNombreValor.Length; i += 2)
        {
            campos[paresNombreValor[i]] = paresNombreValor[i + 1];
        }
        return JsonSerializer.Serialize(campos, Opciones);
    }

    /// <summary>Envía y lee: la operación completa de un método de la interfaz del cliente.</summary>
    public static async Task<T> LlamarAsync<T>(ITransporte transporte, string metodo, string ruta, string? cuerpoJson,
        string? autorizacion) where T : RespuestaBaseDto
    {
        RespuestaHttp respuesta;
        try
        {
            respuesta = await transporte.EnviarAsync(metodo, ruta, cuerpoJson, autorizacion);
        }
        catch (Exception e) when (e is HttpRequestException or TaskCanceledException or TimeoutException)
        {
            throw new ExcepcionServicio(ExcepcionServicio.Red, ExcepcionServicio.MensajeRed, e);
        }
        return Leer<T>(respuesta);
    }

    /// <summary>Convierte la respuesta HTTP en el DTO de la operación, sin decidir si es éxito o error de negocio.</summary>
    public static T Leer<T>(RespuestaHttp respuesta) where T : RespuestaBaseDto
    {
        T? dto = null;
        if (!string.IsNullOrWhiteSpace(respuesta.Cuerpo))
        {
            try
            {
                dto = JsonSerializer.Deserialize<T>(respuesta.Cuerpo, Opciones);
            }
            catch (JsonException)
            {
                dto = null;
            }
        }
        if (dto?.Estado is null)
        {
            throw new ExcepcionServicio(ExcepcionServicio.Srv000,
                ExcepcionServicio.MensajeSrv000 + " (HTTP " + respuesta.Codigo + ")");
        }
        return dto;
    }
}
