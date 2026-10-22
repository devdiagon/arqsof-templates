using System.Net.Http.Headers;
using System.Net.Http.Json;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.api;

/// <summary>
/// Transporte HTTP/JSON genérico: única clase que arma peticiones. No conoce el dominio ni el contrato; los servicios
/// le pasan método, ruta y cuerpo. Recibe un <see cref="HttpClient"/> de larga vida ya configurado.
/// </summary>
public class ClienteApi : IClienteApi
{
    public const string MensajeServidorNoDisponible = "Servidor no disponible, intente más tarde";

    private readonly HttpClient _http;

    public ClienteApi(HttpClient http)
    {
        _http = http;
    }

    /// <summary>HttpClient con URL base, tiempo límite y Accept. Debe crearse una vez y reutilizarse (uno por petición agota los sockets).</summary>
    public static HttpClient CrearHttpClient(string? urlBase = null)
    {
        var http = new HttpClient
        {
            BaseAddress = new Uri(urlBase ?? ConfiguracionApi.ObtenerUrlBase()),
            Timeout = ConfiguracionApi.TiempoLimite
        };
        http.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        return http;
    }

    public async Task<RespuestaHttp> EnviarAsync(HttpMethod metodo, string ruta, object? cuerpo, string? token,
        CancellationToken ct = default)
    {
        using var peticion = new HttpRequestMessage(metodo, ruta);
        // El token va en cada petición, no en DefaultRequestHeaders: un HttpClient compartido mezclaría usuarios.
        if (token is not null)
            peticion.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
        if (cuerpo is not null)
            peticion.Content = JsonContent.Create(cuerpo, cuerpo.GetType());

        try
        {
            // No se usa EnsureSuccessStatusCode: el JSON de un error 4xx/5xx lleva el mensaje que se muestra.
            using var respuesta = await _http.SendAsync(peticion, ct);
            var texto = await respuesta.Content.ReadAsStringAsync(ct);
            return new RespuestaHttp((int)respuesta.StatusCode, texto);
        }
        catch (HttpRequestException ex)
        {
            throw NoDisponible(ex);
        }
        catch (TaskCanceledException ex) when (!ct.IsCancellationRequested)
        {
            // Tiempo límite de HttpClient; si canceló quien llama, la excepción sigue su camino.
            throw NoDisponible(ex);
        }
    }

    private static ExcepcionServicio NoDisponible(Exception causa) =>
        new(MensajeServidorNoDisponible, ExcepcionServicio.CodigoRed, servidorNoDisponible: true, causa: causa);
}
