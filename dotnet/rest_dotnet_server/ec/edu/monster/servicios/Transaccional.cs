using System.Data.Common;
using ec.edu.monster.db;
using Microsoft.Extensions.Logging;

namespace ec.edu.monster.servicios;

/// <summary>
/// Patrón único de conexión y transacción: abre la conexión, inicia la transacción, ejecuta la
/// operación, confirma si todo salió bien, revierte si hubo una excepción (que continúa hacia el
/// controlador) y cierra siempre la conexión. En ADO.NET los repositorios reciben la transacción,
/// así que hasta las lecturas la usan (diferencia de plataforma respecto a JDBC).
/// </summary>
internal static class Transaccional
{
    private static readonly ILogger Logger =
        LoggerFactory.Create(b => b.AddConsole()).CreateLogger(typeof(Transaccional).FullName!);

    public static T Ejecutar<T>(AccesoDB accesoDB, Func<DbTransaction, T> operacion)
    {
        using DbConnection conexion = accesoDB.ObtenerConexion();
        using DbTransaction transaccion = conexion.BeginTransaction();
        try
        {
            T resultado = operacion(transaccion);
            transaccion.Commit();
            return resultado;
        }
        catch
        {
            Revertir(transaccion);
            throw;
        }
    }

    private static void Revertir(DbTransaction transaccion)
    {
        try
        {
            transaccion.Rollback();
        }
        catch (Exception e)
        {
            Logger.LogCritical(e, "No se pudo revertir la transacción");
        }
    }
}
