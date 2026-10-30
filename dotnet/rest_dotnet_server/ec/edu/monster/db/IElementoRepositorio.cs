using System.Data.Common;
using ec.edu.monster.modelo;

namespace ec.edu.monster.db;

/// <summary>
/// EJEMPLO de persistencia de una entidad. En .NET los repositorios reciben la transacción (que lleva su
/// conexión), no la conexión sola (ARQUITECTURA_SERVIDOR.md 4.4).
/// </summary>
public interface IElementoRepositorio
{
    Elemento? BuscarPorCodigo(DbTransaction transaccion, string codigo);

    List<Elemento> Listar(DbTransaction transaccion);

    /// <summary>
    /// Inserta el elemento. Una clave duplicada se traduce a ELE-002 (Conflicto).
    /// </summary>
    void Insertar(DbTransaction transaccion, Elemento elemento);
}
