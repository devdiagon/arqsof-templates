using System.Text;

namespace ec.edu.monster.api;

/// <summary>
/// Transporte real sobre <see cref="HttpClient"/>. Es el único código que abre conexiones.
/// <list type="bullet">
/// <item>Un solo <c>HttpClient</c> de larga vida para todo el programa (<see cref="Crear"/>), con tiempo límite de 10 s.</item>
/// <item>La cabecera <c>Authorization</c> se pone en cada petición, nunca en las cabeceras por defecto del cliente (R8).</item>
/// <item>No usa <c>EnsureSuccessStatusCode</c>: el cuerpo de un HTTP de error lleva el mensaje del servidor (R7).</item>
/// <item>No registra cabeceras ni cuerpos: una credencial no aparece en ningún registro.</item>
/// </list>
/// </summary>
public sealed class TransporteHttp : ITransporte
{
    private static readonly Lazy<HttpClient> ClienteCompartido = new(() =>
        new HttpClient { Timeout = ConfiguracionApi.TiempoLimite });

    private readonly string _urlBase;
    private readonly HttpClient _cliente;

    public TransporteHttp(string urlBase, HttpClient cliente)
    {
        _urlBase = ConfiguracionApi.NormalizarUrl(urlBase);
        _cliente = cliente;
    }

    /// <summary>Transporte listo para usar: URL de <see cref="ConfiguracionApi.UrlBase()"/> y el cliente compartido.</summary>
    public static ITransporte Crear() => new TransporteHttp(ConfiguracionApi.UrlBase(), ClienteCompartido.Value);

    public async Task<RespuestaHttp> EnviarAsync(string metodo, string ruta, string? cuerpoJson, string? autorizacion)
    {
        using var peticion = new HttpRequestMessage(new HttpMethod(metodo), _urlBase + ruta);
        peticion.Headers.TryAddWithoutValidation("Accept", "application/json");
        if (autorizacion is not null)
        {
            peticion.Headers.TryAddWithoutValidation("Authorization", autorizacion);
        }
        if (cuerpoJson is not null)
        {
            peticion.Content = new StringContent(cuerpoJson, Encoding.UTF8, "application/json");
        }

        using HttpResponseMessage respuesta = await _cliente.SendAsync(peticion);
        string cuerpo = await respuesta.Content.ReadAsStringAsync();
        return new RespuestaHttp((int)respuesta.StatusCode, cuerpo);
    }
}
