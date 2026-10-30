using System.Data.Common;
using Microsoft.Data.SqlClient;

namespace ec.edu.monster.db;

/// <summary>
/// Entrega conexiones abiertas a la base de datos del proyecto.
/// El servidor, el usuario y la contraseña son constantes en texto claro (entorno local académico):
/// es lo único que se cambia al copiar esta clase a un proyecto nuevo.
/// Cada operación pide su propia conexión y quien la pide la cierra.
/// </summary>
public class AccesoDB
{
    private const string CadenaConexion =
        "Server=localhost,1433;Database=ejemplo;User Id=ejemplo;Password=admin;" +
        "Encrypt=True;TrustServerCertificate=True";

    /// <summary>
    /// Abre una conexión nueva. Es virtual para poder sustituirlo en las pruebas.
    /// </summary>
    public virtual DbConnection ObtenerConexion()
    {
        var conexion = new SqlConnection(CadenaConexion);
        try
        {
            conexion.Open();
            return conexion;
        }
        catch
        {
            conexion.Dispose();
            throw;
        }
    }
}
