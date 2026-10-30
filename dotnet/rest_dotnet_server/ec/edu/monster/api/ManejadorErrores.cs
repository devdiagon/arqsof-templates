using ec.edu.monster.api.contrato;
using ec.edu.monster.dto;
using ec.edu.monster.excepciones;
using Microsoft.Extensions.Logging;

namespace ec.edu.monster.api;

/// <summary>
/// Manejador de errores de plataforma (R6): todo lo que el framework o un recurso lancen sale con la
/// envoltura estado/codigo/mensaje y el HTTP de la categoría, nunca con una página HTML ni <c>ProblemDetails</c>.
/// Cubre ruta inexistente, verbo no permitido, Content-Type incorrecto, cuerpo mal formado y cualquier
/// excepción no prevista (ERR-500 sin detalles internos). No registra cabeceras ni cuerpos (R8).
/// Es un middleware y debe ser el primero de la canalización (ver Program.cs).
/// </summary>
public class ManejadorErrores
{
    private static readonly ILogger Logger =
        LoggerFactory.Create(b => b.AddConsole()).CreateLogger(typeof(ManejadorErrores).FullName!);

    private readonly RequestDelegate _siguiente;

    public ManejadorErrores(RequestDelegate siguiente)
    {
        _siguiente = siguiente;
    }

    public async Task InvokeAsync(HttpContext contexto)
    {
        try
        {
            await _siguiente(contexto);
        }
        catch (Exception e) when (!contexto.Response.HasStarted)
        {
            contexto.Response.Clear();
            await Responder(contexto, Clasificar(e));
            return;
        }

        // Respuestas vacías del framework (ruta inexistente, verbo no permitido...): se les da la envoltura.
        // Una respuesta con cuerpo o con tipo de contenido propio no se toca. La cabecera Allow del 405 se conserva.
        HttpResponse respuesta = contexto.Response;
        if (!respuesta.HasStarted && respuesta.StatusCode >= 400
            && respuesta.ContentLength is null && respuesta.ContentType is null)
        {
            await Responder(contexto, DeEstado(respuesta.StatusCode));
        }
    }

    /// <summary>Traduce una excepción a la entrada del catálogo que le corresponde.</summary>
    public static ExcepcionMonster Clasificar(Exception excepcion)
    {
        switch (excepcion)
        {
            case ExcepcionMonster monster:
                return monster;
            case BadHttpRequestException mala:
                return DeEstado(mala.StatusCode);
            default:
                Logger.LogError(excepcion, "Error inesperado en la API");
                return new ExcepcionMonster(CodigoError.ERR_500);
        }
    }

    /// <summary>Entrada del catálogo para un HTTP de error que puso el framework.</summary>
    public static ExcepcionMonster DeEstado(int estado) => estado switch
    {
        404 => new ExcepcionMonster(CodigoError.ERR_404),
        405 => new ExcepcionMonster(CodigoError.ERR_405),
        415 => new ExcepcionMonster(CodigoError.ERR_415),
        >= 400 and < 500 => new ExcepcionMonster(CodigoError.DATA_001, ConvencionRest.MensajeCuerpoInvalido),
        _ => new ExcepcionMonster(CodigoError.ERR_500)
    };

    private static async Task Responder(HttpContext contexto, ExcepcionMonster error)
    {
        var cuerpo = new RespuestaBase();
        cuerpo.AplicarEnvoltura(EstadoResultado.ERROR.ToString(), error.Codigo, error.Message);
        await RespuestasHttp.De(cuerpo).ExecuteAsync(contexto);
    }
}
