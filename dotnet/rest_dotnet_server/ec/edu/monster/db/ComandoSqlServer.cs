using System.Data;
using System.Data.Common;
using Microsoft.Data.SqlClient;

namespace ec.edu.monster.db;

/// <summary>
/// Crea los comandos y parámetros de los repositorios. En ADO.NET cada comando debe recibir su
/// transacción explícitamente (diferencia de plataforma respecto a JDBC).
/// </summary>
internal static class ComandoSqlServer
{
    public static SqlCommand Crear(DbTransaction transaccion, string sql) =>
        new(sql, (SqlConnection)transaccion.Connection!, (SqlTransaction)transaccion);

    public static SqlParameter Char(string nombre, int longitud, string? valor) =>
        new(nombre, SqlDbType.Char, longitud) { Value = (object?)valor ?? DBNull.Value };

    public static SqlParameter Varchar(string nombre, int longitud, string valor) =>
        new(nombre, SqlDbType.VarChar, longitud) { Value = valor };

    public static SqlParameter Entero(string nombre, int valor) =>
        new(nombre, SqlDbType.Int) { Value = valor };

    public static SqlParameter Decimal(string nombre, decimal valor) =>
        new(nombre, SqlDbType.Decimal) { Precision = 12, Scale = 2, Value = valor };

    public static SqlParameter Fecha(string nombre, DateOnly valor) =>
        new(nombre, SqlDbType.Date) { Value = valor.ToDateTime(TimeOnly.MinValue) };
}
