using System.Data.Common;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using Microsoft.Data.SqlClient;

namespace ec.edu.monster.db;

/// <summary>
/// EJEMPLO de repositorio: SQL como constantes, parámetros con nombre, nunca abre ni cierra la conexión.
/// </summary>
public class ElementoRepositorioSqlServer : IElementoRepositorio
{
    // Claves duplicadas de SQL Server: 2627 (restricción única o clave primaria) y 2601 (índice único).
    private const int SqlServerClaveDuplicada = 2627;
    private const int SqlServerIndiceUnicoDuplicado = 2601;

    private const string SqlCampos = "SELECT chr_elecodigo, vch_elenombre, dec_elevalor FROM elemento";
    private const string SqlPorCodigo = SqlCampos + " WHERE chr_elecodigo = @codigo";
    private const string SqlListar = SqlCampos + " ORDER BY chr_elecodigo";
    private const string SqlInsertar =
        "INSERT INTO elemento (chr_elecodigo, vch_elenombre, dec_elevalor) VALUES (@codigo, @nombre, @valor)";

    public Elemento? BuscarPorCodigo(DbTransaction transaccion, string codigo)
    {
        using SqlCommand comando = ComandoSqlServer.Crear(transaccion, SqlPorCodigo);
        comando.Parameters.Add(ComandoSqlServer.Char("@codigo", Elemento.LongitudCodigo, codigo));
        using SqlDataReader lector = comando.ExecuteReader();
        return lector.Read() ? Mapear(lector) : null;
    }

    public List<Elemento> Listar(DbTransaction transaccion)
    {
        var elementos = new List<Elemento>();
        using SqlCommand comando = ComandoSqlServer.Crear(transaccion, SqlListar);
        using SqlDataReader lector = comando.ExecuteReader();
        while (lector.Read())
        {
            elementos.Add(Mapear(lector));
        }
        return elementos;
    }

    public void Insertar(DbTransaction transaccion, Elemento elemento)
    {
        try
        {
            using SqlCommand comando = ComandoSqlServer.Crear(transaccion, SqlInsertar);
            comando.Parameters.Add(ComandoSqlServer.Char("@codigo", Elemento.LongitudCodigo, elemento.Codigo));
            comando.Parameters.Add(ComandoSqlServer.Varchar("@nombre", Elemento.LongitudMaximaNombre, elemento.Nombre));
            comando.Parameters.Add(ComandoSqlServer.Decimal("@valor", elemento.Valor));
            comando.ExecuteNonQuery();
        }
        catch (SqlException e) when (e.Number is SqlServerClaveDuplicada or SqlServerIndiceUnicoDuplicado)
        {
            throw new ExcepcionMonster(CodigoError.ELE_002);
        }
    }

    private static Elemento Mapear(SqlDataReader lector) => new(
        lector.GetString(lector.GetOrdinal("chr_elecodigo")),
        lector.GetString(lector.GetOrdinal("vch_elenombre")),
        lector.GetDecimal(lector.GetOrdinal("dec_elevalor")));
}
