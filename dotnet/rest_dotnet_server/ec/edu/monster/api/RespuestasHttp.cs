using System.Text.Encodings.Web;
using System.Text.Json;
using System.Text.Json.Serialization;
using ec.edu.monster.api.contrato;

namespace ec.edu.monster.api;

/// <summary>
/// Respuesta HTTP de un tipo de contrato: el HTTP sale de la categoría del error (R7) y el cuerpo es JSON UTF-8
/// (R3). Es el único lugar que serializa. Se serializa con el tipo real de la respuesta, no con el de la base.
/// Si el proyecto agrega autenticación, aquí se completa lo que su mecanismo exija en un 401
/// (por ejemplo la cabecera <c>WWW-Authenticate</c>, R8).
/// </summary>
public sealed class RespuestasHttp : IResult
{
    /// <summary>Nombres fijados en el contrato (<c>JsonPropertyName</c>), sin valores nulos y con tildes literales.</summary>
    public static readonly JsonSerializerOptions Opciones = new()
    {
        DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull,
        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping
    };

    public RespuestaBase Cuerpo { get; }
    public int Http { get; }

    private RespuestasHttp(RespuestaBase cuerpo)
    {
        Cuerpo = cuerpo;
        Http = CodigoHttp.DeRespuesta(cuerpo.Estado, cuerpo.Codigo);
    }

    public static RespuestasHttp De(RespuestaBase respuesta) => new(respuesta);

    /// <summary>JSON del cuerpo, con el tipo real de la respuesta.</summary>
    public string Json() => JsonSerializer.Serialize(Cuerpo, Cuerpo.GetType(), Opciones);

    public async Task ExecuteAsync(HttpContext contexto)
    {
        HttpResponse respuesta = contexto.Response;
        respuesta.StatusCode = Http;
        respuesta.ContentType = ConvencionRest.JsonUtf8;
        await respuesta.WriteAsync(Json());
    }
}
