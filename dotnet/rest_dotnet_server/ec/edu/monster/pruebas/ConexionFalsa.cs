using System.Data;
using System.Data.Common;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Conexión simulada que registra cómo termina la transacción. Moq no puede simular con comodidad
/// los miembros protegidos de <see cref="DbConnection"/>, así que se escribe a mano.
/// </summary>
public sealed class ConexionFalsa : DbConnection
{
    public TransaccionFalsa Transaccion { get; }
    public bool Cerrada { get; private set; }

    public ConexionFalsa(bool fallarAlRevertir = false)
    {
        Transaccion = new TransaccionFalsa(this, fallarAlRevertir);
    }

    protected override void Dispose(bool disposing)
    {
        Cerrada = true;
        base.Dispose(disposing);
    }

    protected override DbTransaction BeginDbTransaction(IsolationLevel isolationLevel) => Transaccion;

#pragma warning disable CS8765 // la nulabilidad de la clase base no importa en un doble
    public override string ConnectionString { get; set; } = "";
#pragma warning restore CS8765
    public override string Database => "falsa";
    public override string DataSource => "falsa";
    public override string ServerVersion => "0";
    public override ConnectionState State => Cerrada ? ConnectionState.Closed : ConnectionState.Open;
    public override void ChangeDatabase(string databaseName) { }
    public override void Close() { }
    public override void Open() { }
    protected override DbCommand CreateDbCommand() => throw new NotSupportedException();
}

/// <summary>Transacción simulada: cuenta las confirmaciones y las reversiones.</summary>
public sealed class TransaccionFalsa : DbTransaction
{
    private readonly ConexionFalsa _conexion;
    private readonly bool _fallarAlRevertir;

    public int Confirmaciones { get; private set; }
    public int Reversiones { get; private set; }

    public TransaccionFalsa(ConexionFalsa conexion, bool fallarAlRevertir)
    {
        _conexion = conexion;
        _fallarAlRevertir = fallarAlRevertir;
    }

    protected override DbConnection DbConnection => _conexion;
    public override IsolationLevel IsolationLevel => IsolationLevel.ReadCommitted;

    public override void Commit() => Confirmaciones++;

    public override void Rollback()
    {
        Reversiones++;
        if (_fallarAlRevertir)
        {
            throw new InvalidOperationException("rollback fallido");
        }
    }
}
