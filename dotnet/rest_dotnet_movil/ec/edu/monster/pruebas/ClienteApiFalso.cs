using System.Text.Json;
using ec.edu.monster.api;
using ec.edu.monster.dto;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Doble escrito a mano de <see cref="IClienteApi"/>: devuelve <see cref="RespuestaHttp"/> simuladas y registra las
/// llamadas. Sirve para probar servicios y controladores sin servidor; las pruebas del dominio lo reutilizan.
/// </summary>
public class ClienteApiFalso : IClienteApi
{
    public record Llamada(HttpMethod Metodo, string Ruta, object? Cuerpo, string? Token);

    public List<Llamada> Llamadas { get; } = [];

    /// <summary>Respuesta por llamada. Por defecto, un éxito vacío; las pruebas del dominio lo reemplazan por ruta.</summary>
    public Func<Llamada, RespuestaHttp> Responder { get; set; } = _ => Ok(new RespuestaBaseDto());

    public Task<RespuestaHttp> EnviarAsync(HttpMethod metodo, string ruta, object? cuerpo, string? token,
        CancellationToken ct = default)
    {
        var llamada = new Llamada(metodo, ruta, cuerpo, token);
        Llamadas.Add(llamada);
        return Task.FromResult(Responder(llamada));
    }

    /// <summary>Hace que toda llamada devuelva esta respuesta.</summary>
    public void Siempre(RespuestaHttp respuesta) => Responder = _ => respuesta;

    public static RespuestaHttp Ok(RespuestaBaseDto dto)
    {
        dto.Estado = "OK";
        dto.Codigo = "200";
        dto.Mensaje = "Correcto";
        return new RespuestaHttp(200, JsonSerializer.Serialize(dto, dto.GetType()));
    }

    /// <summary>Error de plataforma: el cuerpo solo trae estado, código y mensaje.</summary>
    public static RespuestaHttp Error(int http, string codigo, string mensaje) =>
        new(http, JsonSerializer.Serialize(new RespuestaBaseDto { Estado = "ERROR", Codigo = codigo, Mensaje = mensaje }));
}
