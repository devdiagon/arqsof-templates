using ec.edu.monster.dto;
using ec.edu.monster.excepciones;
using Microsoft.Extensions.Logging;

namespace ec.edu.monster.controlador;

/// <summary>
/// Único punto del servidor que captura excepciones. Ejecuta una operación y siempre devuelve
/// un Resultado: de éxito, de error de negocio (ExcepcionMonster con su código y mensaje) o,
/// ante cualquier otra excepción, ERR-500 sin exponer detalles internos.
/// </summary>
public abstract class ControladorBase
{
    private static readonly ILogger Logger =
        LoggerFactory.Create(b => b.AddConsole()).CreateLogger(typeof(ControladorBase).FullName!);

    protected Resultado<T> Ejecutar<T>(Func<T> operacion)
    {
        try
        {
            return Resultado.Exito(operacion());
        }
        catch (ExcepcionMonster e)
        {
            return Resultado.Error<T>(e.Codigo, e.Message);
        }
        catch (Exception e)
        {
            Logger.LogError(e, "Error inesperado");
            return Resultado.Error<T>(CodigoError.ERR_500.Codigo, CodigoError.ERR_500.Mensaje);
        }
    }
}
